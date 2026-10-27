"""Módulo de reposição do Mente Fria (US14) — Contrato v2, §1.6.

Lê UM JSON na stdin e escreve UM JSON na stdout (UTF-8). Não acessa banco nem rede de dados.
Código de saída 0 = há JSON válido no stdout (inclusive com status "erro").
Código diferente de 0 = falha técnica (detalhe no stderr).

Para cada ingrediente i (escolhido pelo Java: os 8 ou 15 mais consumidos no período):
  c_i = consumo no período, S_i = saldo VÁLIDO, qmin_i = quantidade mínima, k_i = custo unitário.
  mínimo a comprar   min_i = max(0, qmin_i - S_i)         (levar o estoque ao mínimo)
  necessidade        nec_i = max(0, c_i + qmin_i - S_i)   (repetir o consumo e manter o mínimo)
  As duas são arredondadas PARA CIMA em centésimos (2 casas), para nunca ficar abaixo do mínimo.

Se soma(k_i * min_i) > orçamento: orçamento insuficiente -> devolve os mínimos e quanto falta (sem solver).
Senão (Programação Linear Inteira mista; x_i em centésimos, inteiro):
  Estágio 1: maximizar t (menor cobertura x_i / nec_i)  s.a.  min_i <= x_i <= nec_i,  x_i >= t * nec_i,
             soma(k_i * x_i) <= orçamento.  t é conferido em frações exatas.
  Estágio 2: mantendo cada x_i >= teto(t* * nec_i), maximizar o gasto soma(k_i * x_i) (usa a sobra do orçamento).
  Estágio 3 (desempate técnico): do ingrediente mais consumido ao menos consumido (empate: menor id),
             maximizar x_i mantendo o gasto do estágio 2 e fixando os já decididos.
Tudo em INTEIROS: quantidades em milésimos (entrada) e centésimos (compra), dinheiro em centavos;
o gasto fica em "centavos x centésimos" (1e-4 R$) e é conferido em aritmética inteira.
"""
import json
import math
import sys
from decimal import Decimal, ROUND_HALF_UP
from fractions import Fraction

import gurobipy as gp
from gurobipy import GRB

# Mesmo ambiente Gurobi e mesmas regras de conversão do otimizador de produção (um só lugar para a licença).
from otimizador import ErroDeDominio, FalhaTecnica, _criar_env, _dec, _inteiro_escalado


def _teto_div(a, b):
    return -(-a // b)


# --------------------------------------------------------------------------- validação
def validar_e_converter(entrada):
    try:
        orcamento = _dec(entrada["orcamento"], 2, "Orçamento inválido")
    except (KeyError, TypeError):
        raise ErroDeDominio("Orçamento inválido")
    if orcamento < 0:
        raise ErroDeDominio("Orçamento inválido")
    ingredientes = entrada.get("ingredientes")
    if not isinstance(ingredientes, list) or len(ingredientes) == 0:
        raise ErroDeDominio("Nenhum ingrediente consumido no período")

    itens, vistos = [], set()
    for ing in ingredientes:
        try:
            ing_id = ing["id"]
            c = _dec(ing["consumo_periodo"], 3, "Números inválidos em ingredientes")
            s = _dec(ing["quantidade_estoque"], 3, "Números inválidos em ingredientes")
            qmin = _dec(ing["quantidade_minima"], 3, "Números inválidos em ingredientes")
            k = _dec(ing["custo_unitario"], 2, "Ingrediente sem custo unitário válido")
        except (KeyError, TypeError):
            raise ErroDeDominio("Números inválidos em ingredientes")
        if isinstance(ing_id, bool) or not isinstance(ing_id, int) or ing_id in vistos:
            raise ErroDeDominio("Números inválidos em ingredientes")
        if c < 0 or s < 0 or qmin < 0 or k < 0:
            raise ErroDeDominio("Números inválidos em ingredientes")
        vistos.add(ing_id)
        c_m, s_m, q_m = (_inteiro_escalado(v, 3) for v in (c, s, qmin))     # milésimos
        itens.append({
            "id": ing_id,
            "consumo": c_m,
            "custo": _inteiro_escalado(k, 2),                                 # centavos
            "min": _teto_div(max(0, q_m - s_m), 10),                          # centésimos, para cima
            "nec": _teto_div(max(0, c_m + q_m - s_m), 10),                    # centésimos, para cima
        })
    return itens, _inteiro_escalado(orcamento, 2) * 100                      # orçamento em 1e-4 R$


# --------------------------------------------------------------------------- verificação inteira
def _gasto(plano, itens):
    return sum(i["custo"] * plano[i["id"]] for i in itens)


def _cobertura(plano, itens):
    razoes = [Fraction(plano[i["id"]], i["nec"]) for i in itens if i["nec"] > 0]
    return min(razoes) if razoes else Fraction(1)


def _verificar(plano, itens, orcamento):
    for i in itens:
        if not i["min"] <= plano[i["id"]] <= i["nec"]:
            raise FalhaTecnica("Quantidade fora dos limites")
    if _gasto(plano, itens) > orcamento:
        raise FalhaTecnica("Plano estoura o orçamento")


def _ordem_desempate(itens):
    return sorted(itens, key=lambda i: (-i["consumo"], i["id"]))


# --------------------------------------------------------------------------- Gurobi
def resolver(itens, orcamento):
    """Devolve (plano em centésimos, cobertura exata). Supõe orçamento suficiente para os mínimos."""
    plano = {i["id"]: i["min"] for i in itens}
    variaveis = [i for i in itens if i["nec"] > i["min"]]
    if not variaveis:
        return plano, _cobertura(plano, itens)
    with _criar_env() as env, gp.Model(env=env) as m:
        m.setParam("MIPGap", 0)
        m.setParam("MIPGapAbs", 0)
        m.setParam("IntFeasTol", 1e-9)
        m.setParam("FeasibilityTol", 1e-9)
        x = {i["id"]: m.addVar(vtype=GRB.INTEGER, lb=i["min"], ub=i["nec"]) for i in variaveis}
        expr = {i["id"]: (x[i["id"]] if i["id"] in x else i["min"]) for i in itens}
        gasto_expr = gp.quicksum(i["custo"] * expr[i["id"]] for i in itens)
        m.addConstr(gasto_expr <= orcamento, name="orcamento")

        # ---- Estágio 1: maior cobertura mínima
        t = m.addVar(lb=0, ub=1, name="t")
        for i in itens:
            if i["nec"] > 0:
                m.addConstr(expr[i["id"]] >= i["nec"] * t)
        m.setObjective(t, GRB.MAXIMIZE)
        m.optimize()
        if m.Status != GRB.OPTIMAL:
            raise FalhaTecnica("Estágio 1 sem solução ótima")
        for i in variaveis:
            plano[i["id"]] = int(round(x[i["id"]].X))
        _verificar(plano, itens, orcamento)
        tstar = _cobertura(plano, itens)
        # Conferência exata: dá para aumentar a cobertura? (cada x_i > t* . nec_i, i.e. x_i >= piso(t* . nec_i) + 1)
        if tstar < 1:
            melhor = {i["id"]: max(i["min"], math.floor(tstar * i["nec"]) + 1) if i["nec"] > 0 else i["min"]
                      for i in itens}
            if all(melhor[i["id"]] <= i["nec"] for i in itens) and _gasto(melhor, itens) <= orcamento:
                raise FalhaTecnica("Cobertura do solver não é a máxima (tolerância numérica)")

        # ---- Estágio 2: mantendo a cobertura t*, usar o máximo do orçamento
        m.remove(t)
        for i in variaveis:
            if i["nec"] > 0:
                x[i["id"]].LB = max(i["min"], math.ceil(tstar * i["nec"]))
        m.setObjective(gasto_expr, GRB.MAXIMIZE)
        m.optimize()
        if m.Status != GRB.OPTIMAL:
            raise FalhaTecnica("Estágio 2 sem solução ótima")
        for i in variaveis:
            plano[i["id"]] = int(round(x[i["id"]].X))
        _verificar(plano, itens, orcamento)
        gstar = _gasto(plano, itens)
        if abs(m.ObjVal - gstar) > 1e-6 * max(1, gstar):
            raise FalhaTecnica("Gasto do solver diverge do cálculo inteiro")

        # ---- Estágio 3: desempate técnico (mais consumido primeiro), mantendo o gasto g*
        m.addConstr(gasto_expr >= gstar, name="manter_gasto")
        for i in _ordem_desempate(variaveis):
            m.setObjective(x[i["id"]], GRB.MAXIMIZE)
            m.optimize()
            if m.Status != GRB.OPTIMAL:
                raise FalhaTecnica("Estágio 3 sem solução ótima")
            v = int(round(x[i["id"]].X))
            x[i["id"]].LB = v
            x[i["id"]].UB = v
            plano[i["id"]] = v
        _verificar(plano, itens, orcamento)
        if _gasto(plano, itens) != gstar or _cobertura(plano, itens) != tstar:
            raise FalhaTecnica("Desempate alterou o gasto ou a cobertura")
        return plano, tstar


# --------------------------------------------------------------------------- saída
def _reais(valor_1e4):
    return float((Decimal(valor_1e4) / Decimal(10000)).quantize(Decimal("0.01"), ROUND_HALF_UP))


def _quantidade(centesimos):
    return float(Decimal(centesimos) / 100)


def _montar(itens, plano):
    linhas = [{
        "ingrediente_id": i["id"],
        "quantidade_sugerida": _quantidade(plano[i["id"]]),
        "quantidade_minima_compra": _quantidade(i["min"]),
        "quantidade_necessaria": _quantidade(i["nec"]),
        "custo_estimado": _reais(i["custo"] * plano[i["id"]]),
        "_ordem": i["custo"] * plano[i["id"]],
    } for i in itens]
    linhas.sort(key=lambda l: (-l["_ordem"], l["ingrediente_id"]))           # maior custo de compra primeiro
    for l in linhas:
        del l["_ordem"]
    return linhas


def processar(entrada):
    try:
        itens, orcamento = validar_e_converter(entrada)
    except ErroDeDominio as e:
        return {"status": "erro", "mensagem": str(e)}
    minimos = {i["id"]: i["min"] for i in itens}
    custo_minimos = _gasto(minimos, itens)
    if custo_minimos > orcamento:
        return {
            "status": "sucesso",
            "orcamento_suficiente": False,
            "custo_total": _reais(custo_minimos),
            "valor_faltante": _reais(custo_minimos - orcamento),
            "cobertura_minima": None,
            "itens": _montar(itens, minimos),
        }
    plano, cobertura = resolver(itens, orcamento)
    return {
        "status": "sucesso",
        "orcamento_suficiente": True,
        "custo_total": _reais(_gasto(plano, itens)),
        "valor_faltante": 0.0,
        "cobertura_minima": round(float(cobertura), 4),          # 0 a 1 (1 = compra toda a necessidade)
        "itens": _montar(itens, plano),
    }


def main():
    sys.stdin.reconfigure(encoding="utf-8-sig")      # aceita BOM (A8)
    sys.stdout.reconfigure(encoding="utf-8")
    try:
        entrada = json.load(sys.stdin, parse_float=Decimal)
    except json.JSONDecodeError:
        print(json.dumps({"status": "erro", "mensagem": "JSON de entrada inválido"}, ensure_ascii=False))
        return 0
    if not isinstance(entrada, dict):
        print(json.dumps({"status": "erro", "mensagem": "JSON de entrada inválido"}, ensure_ascii=False))
        return 0
    print(json.dumps(processar(entrada), ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())

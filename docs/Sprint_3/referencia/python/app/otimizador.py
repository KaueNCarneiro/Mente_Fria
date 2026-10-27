"""Módulo de otimização do Mente Fria — Contrato v2 (maximiza LUCRO; modo eco para o Hello World).

Lê UM JSON na stdin e escreve UM JSON na stdout (UTF-8). Não acessa banco nem rede de dados.
Código de saída 0 = há JSON válido no stdout (inclusive com status "erro").
Código diferente de 0 = falha técnica (detalhe no stderr).

Modelo (Programação Linear Inteira pura, dois estágios):
  Estágio 1: maximizar L = soma(m_p * x_p)  sujeito a  soma(q_ip * x_p) <= S_i,  x_p inteiro >= 0.
  Estágio 2 (desempate técnico): para cada produto em ordem crescente de id, maximizar x_p
             mantendo L >= L*, fixando os produtos já decididos.
Tudo é escalado para INTEIROS: quantidades em milésimos, preços/custos em centavos.
O lucro por copo fica em "centavos x milésimos" (1e-5 R$) e é conferido em aritmética inteira.
"""
import json
import os
import sys
from decimal import Decimal, ROUND_HALF_UP

import gurobipy as gp
from gurobipy import GRB


class ErroDeDominio(Exception):
    """Dado inválido: vira {"status": "erro", "mensagem": ...} (exit code 0)."""


class FalhaTecnica(Exception):
    """Resultado do solver não confere na verificação inteira: exit code != 0."""


# --------------------------------------------------------------------------- conversão
def _dec(valor, casas, erro):
    """Aceita int/Decimal (nunca bool nem texto) com no máximo `casas` casas decimais."""
    if isinstance(valor, bool) or not isinstance(valor, (int, Decimal)):
        raise ErroDeDominio(erro)
    d = Decimal(valor)
    if not d.is_finite() or d != d.quantize(Decimal(1).scaleb(-casas)):
        raise ErroDeDominio(erro)
    return d


def _inteiro_escalado(d, casas):
    return int((d * (10 ** casas)).to_integral_value())


def validar_e_converter(entrada):
    produtos = entrada.get("produtos")
    ingredientes = entrada.get("ingredientes")
    if not isinstance(produtos, list) or len(produtos) == 0:
        raise ErroDeDominio("Nenhum produto ativo para otimização")
    if not isinstance(ingredientes, list):
        raise ErroDeDominio("Lista de ingredientes ausente ou inválida")

    estoque, custo = {}, {}          # id -> milésimos | centavos
    for ing in ingredientes:
        try:
            ing_id = ing["id"]
            s = _dec(ing["quantidade_estoque"], 3, "Números inválidos em ingredientes")
            k = _dec(ing["custo_unitario"], 2, "Ingrediente sem custo unitário válido")
        except (KeyError, TypeError):
            raise ErroDeDominio("Números inválidos em ingredientes")
        if s < 0 or k < 0 or ing_id in estoque:
            raise ErroDeDominio("Números inválidos em ingredientes")
        estoque[ing_id] = _inteiro_escalado(s, 3)
        custo[ing_id] = _inteiro_escalado(k, 2)

    lista = []
    ids_vistos = set()
    for p in produtos:
        pid = p.get("id")
        comp = p.get("composicao")
        if not isinstance(comp, list) or len(comp) == 0:
            raise ErroDeDominio(f"Produto {pid} sem composição")
        if pid in ids_vistos:
            raise ErroDeDominio("Números inválidos em produtos")
        ids_vistos.add(pid)
        itens, usados = {}, set()
        for item in comp:
            ing_id = item.get("ingrediente_id")
            if ing_id not in estoque:
                raise ErroDeDominio("Composição cita ingrediente ausente da lista")
            try:
                q = _dec(item["quantidade_utilizada"], 3, "Números inválidos em composição")
            except (KeyError, TypeError):
                raise ErroDeDominio("Números inválidos em composição")
            if q <= 0 or ing_id in usados:
                raise ErroDeDominio("Números inválidos em composição")
            usados.add(ing_id)
            itens[ing_id] = _inteiro_escalado(q, 3)
        try:
            r = _dec(p["preco_venda"], 2, "Números inválidos em produtos")
        except (KeyError, TypeError):
            raise ErroDeDominio("Números inválidos em produtos")
        if r <= 0:
            raise ErroDeDominio("Números inválidos em produtos")
        preco = _inteiro_escalado(r, 2)
        # lucro por copo em "centavos x milésimos": preço*1000 - soma(custo*quantidade)
        lucro = preco * 1000 - sum(custo[i] * q for i, q in itens.items())
        teto = min(estoque[i] // q for i, q in itens.items())
        lista.append({"id": pid, "preco": preco, "itens": itens,
                      "lucro": lucro, "teto": teto if lucro > 0 else 0})
    lista.sort(key=lambda x: x["id"])
    return lista, estoque


# --------------------------------------------------------------------------- Gurobi
def _criar_env():
    env = gp.Env(empty=True)
    env.setParam("OutputFlag", 0)          # nada de banner no stdout
    # Licença WLS por variáveis de ambiente (produção, Alternativa C); sem elas, usa gurobi.lic.
    if os.environ.get("GRB_WLSACCESSID"):
        env.setParam("WLSACCESSID", os.environ["GRB_WLSACCESSID"])
        env.setParam("WLSSECRET", os.environ["GRB_WLSSECRET"])
        env.setParam("LICENSEID", int(os.environ["GRB_LICENSEID"]))
    env.start()
    return env


def _verificar(plano, produtos, estoque, lucro_esperado=None):
    """Confere em aritmética inteira. Divergência = falha técnica, nunca plano errado."""
    consumo = {i: 0 for i in estoque}
    lucro = 0
    for p in produtos:
        x = plano[p["id"]]
        if x < 0 or x > p["teto"]:
            raise FalhaTecnica("Quantidade fora dos limites")
        lucro += p["lucro"] * x
        for i, q in p["itens"].items():
            consumo[i] += q * x
    if any(consumo[i] > estoque[i] for i in estoque):
        raise FalhaTecnica("Plano viola o estoque")
    if lucro_esperado is not None and lucro != lucro_esperado:
        raise FalhaTecnica("Lucro do plano diverge do estágio 1")
    return lucro


def resolver(produtos, estoque):
    ativos = [p for p in produtos if p["teto"] > 0]
    plano = {p["id"]: 0 for p in produtos}
    if not ativos:
        return plano, 0
    with _criar_env() as env, gp.Model(env=env) as m:
        m.setParam("MIPGap", 0)
        m.setParam("MIPGapAbs", 0)
        m.setParam("IntFeasTol", 1e-9)
        m.setParam("FeasibilityTol", 1e-9)
        x = {p["id"]: m.addVar(vtype=GRB.INTEGER, lb=0, ub=p["teto"]) for p in ativos}
        for i in estoque:
            termos = [(p["itens"][i], x[p["id"]]) for p in ativos if i in p["itens"]]
            if termos:
                m.addConstr(gp.quicksum(q * v for q, v in termos) <= estoque[i])
        lucro_expr = gp.quicksum(p["lucro"] * x[p["id"]] for p in ativos)
        # ---- Estágio 1
        m.setObjective(lucro_expr, GRB.MAXIMIZE)
        m.optimize()
        if m.Status != GRB.OPTIMAL:
            raise FalhaTecnica("Estágio 1 sem solução ótima")
        parcial = {p["id"]: int(round(x[p["id"]].X)) for p in ativos}
        plano.update(parcial)
        lstar = _verificar(plano, produtos, estoque)
        if abs(m.ObjVal - lstar) > 1e-6 * max(1, abs(lstar)):
            raise FalhaTecnica("Objetivo do solver diverge do cálculo inteiro")
        # ---- Estágio 2 (desempate: ordem crescente de id, maximiza copos)
        m.addConstr(lucro_expr >= lstar, name="manter_lucro")
        for p in ativos:
            m.setObjective(x[p["id"]], GRB.MAXIMIZE)
            m.optimize()
            if m.Status != GRB.OPTIMAL:
                raise FalhaTecnica("Estágio 2 sem solução ótima")
            v = int(round(x[p["id"]].X))
            x[p["id"]].LB = v
            x[p["id"]].UB = v
            plano[p["id"]] = v
        _verificar(plano, produtos, estoque, lucro_esperado=lstar)
        return plano, lstar


def _dinheiro(inteiro, escala):
    return float((Decimal(inteiro) / Decimal(escala)).quantize(Decimal("0.01"), ROUND_HALF_UP))


def processar(entrada):
    if "echo" in entrada and "produtos" not in entrada:                   # modo eco: prova a ponte Java <-> Python
        texto = entrada.get("echo")
        if not isinstance(texto, str):
            return {"status": "erro", "mensagem": "Campo 'echo' ausente ou inválido"}
        return {"status": "sucesso", "echo": texto.upper()}
    try:
        produtos, estoque = validar_e_converter(entrada)
    except ErroDeDominio as e:
        return {"status": "erro", "mensagem": str(e)}
    plano, lstar = resolver(produtos, estoque)
    receita = sum(p["preco"] * plano[p["id"]] for p in produtos)          # centavos
    consumo = {}                                                          # milésimos
    for p in produtos:
        for ing_id, q in p["itens"].items():
            consumo[ing_id] = consumo.get(ing_id, 0) + q * plano[p["id"]]
    return {
        "status": "sucesso",
        "receita_estimada": _dinheiro(receita, 100),
        "margem_estimada": _dinheiro(lstar, 100000),                        # 1e-5 R$
        "recomendacao": [{"produto_id": p["id"], "quantidade": plano[p["id"]]} for p in produtos],
        "consumo": [{"ingrediente_id": i, "quantidade": float(Decimal(q) / 1000)}
                    for i, q in sorted(consumo.items()) if q > 0],
    }


def main():
    # utf-8-sig: aceita a entrada com ou sem BOM. O pipe do PowerShell envia um BOM no início;
    # sem isto, o json.load falha com "JSON de entrada inválido" (verificado em 26/10/2026).
    sys.stdin.reconfigure(encoding="utf-8-sig")
    sys.stdout.reconfigure(encoding="utf-8")
    try:
        # parse_float=Decimal: o JSON nunca passa por float (evita 0.1 + 0.2 != 0.3)
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

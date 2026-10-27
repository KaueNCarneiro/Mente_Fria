"""Oráculo da reposição (US14): força bruta com frações exatas, SEM solver. Só para instâncias pequenas.

Regras (as mesmas do módulo, escritas de outro jeito para o resultado esperado não vir do próprio módulo):
  min = teto((qmin - S) / 0,01) e nec = teto((c + qmin - S) / 0,01), em centésimos, nunca negativos.
  Orçamento insuficiente se soma(k * min) > orçamento.
  Senão, entre todos os planos min <= x <= nec que cabem no orçamento, vence:
    1) a maior cobertura mínima (x / nec, só onde nec > 0);
    2) depois, o maior gasto;
    3) depois, o maior x do ingrediente mais consumido (empate: menor id), depois o seguinte, e assim por diante.
"""
from decimal import Decimal
from fractions import Fraction
from itertools import product as produto_cartesiano


def _centesimos_para_cima(valor):
    v = Decimal(str(valor)) * 100
    inteiro = int(v)
    return max(0, inteiro + (1 if v > inteiro else 0))


def oraculo_reposicao(entrada, limite=300_000):
    orc = Decimal(str(entrada["orcamento"]))
    itens = []
    for i in entrada["ingredientes"]:
        c, s = Decimal(str(i["consumo_periodo"])), Decimal(str(i["quantidade_estoque"]))
        q, k = Decimal(str(i["quantidade_minima"])), Decimal(str(i["custo_unitario"]))
        itens.append({"id": i["id"], "c": c, "k": k,
                      "min": _centesimos_para_cima(max(Decimal(0), q - s)),
                      "nec": _centesimos_para_cima(max(Decimal(0), c + q - s))})
    gasto = lambda x: sum(it["k"] * Decimal(v) / 100 for it, v in zip(itens, x))
    minimos = tuple(it["min"] for it in itens)
    if gasto(minimos) > orc:
        return {"suficiente": False, "plano": {it["id"]: it["min"] for it in itens}, "gasto": gasto(minimos)}

    espaco = 1
    for it in itens:
        espaco *= it["nec"] - it["min"] + 1
    if espaco > limite:
        raise ValueError("instância grande demais para força bruta")
    ordem = sorted(range(len(itens)), key=lambda j: (-itens[j]["c"], itens[j]["id"]))
    melhor_chave, melhor_x = None, None
    for x in produto_cartesiano(*[range(it["min"], it["nec"] + 1) for it in itens]):
        g = gasto(x)
        if g > orc:
            continue
        razoes = [Fraction(v, it["nec"]) for it, v in zip(itens, x) if it["nec"] > 0]
        chave = (min(razoes) if razoes else Fraction(1), g, tuple(x[j] for j in ordem))
        if melhor_chave is None or chave > melhor_chave:
            melhor_chave, melhor_x = chave, x
    return {"suficiente": True, "plano": {it["id"]: v for it, v in zip(itens, melhor_x)},
            "gasto": melhor_chave[1], "cobertura": melhor_chave[0]}

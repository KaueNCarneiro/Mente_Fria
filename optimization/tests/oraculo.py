"""Oráculo de teste: força bruta com Decimal, SEM solver. Só para instâncias pequenas.

Serve para o resultado esperado não ser calculado pelo próprio otimizador (Plano de Testes, seção 1).
Desempate igual ao do modelo: entre os planos de maior lucro, vence o lexicograficamente
maior em (x_id_menor, x_id_seguinte, ...).
"""
from decimal import Decimal
from itertools import product as produto_cartesiano


def oraculo(entrada, limite=300_000):
    estoque = {i["id"]: Decimal(str(i["quantidade_estoque"])) for i in entrada["ingredientes"]}
    custo = {i["id"]: Decimal(str(i["custo_unitario"])) for i in entrada["ingredientes"]}
    prods = sorted(entrada["produtos"], key=lambda p: p["id"])
    info = []
    for p in prods:
        q = {c["ingrediente_id"]: Decimal(str(c["quantidade_utilizada"])) for c in p["composicao"]}
        lucro = Decimal(str(p["preco_venda"])) - sum(custo[i] * v for i, v in q.items())
        teto = min(int(estoque[i] // v) for i, v in q.items()) if lucro > 0 else 0
        info.append((p["id"], Decimal(str(p["preco_venda"])), q, lucro, teto))
    espaco = 1
    for *_, teto in info:
        espaco *= teto + 1
    if espaco > limite:
        raise ValueError("instância grande demais para força bruta")
    melhor, melhor_x = None, None
    for x in produto_cartesiano(*[range(t + 1) for *_, t in info]):
        consumo = {}
        for (pid, preco, q, lucro, teto), n in zip(info, x):
            for i, v in q.items():
                consumo[i] = consumo.get(i, Decimal(0)) + v * n
        if any(consumo[i] > estoque[i] for i in consumo):
            continue
        L = sum(lucro * n for (_, _, _, lucro, _), n in zip(info, x))
        if melhor is None or L > melhor or (L == melhor and x > melhor_x):
            melhor, melhor_x = L, x
    receita = sum(preco * n for (_, preco, *_), n in zip(info, melhor_x))
    return {"plano": {pid: n for (pid, *_), n in zip(info, melhor_x)}, "margem": melhor, "receita": receita}

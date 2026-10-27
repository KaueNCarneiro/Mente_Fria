import os, sys, threading
from datetime import date
from decimal import Decimal as D
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from fefo_referencia import *
HOJE = date(2026, 10, 13); U = 1

def pedido(ings_qtds, resultado, idx):
    """Um 'pedido': trava ingredientes em ordem de id e baixa cada um, tudo em UMA transação."""
    c = ligar(); cur = c.cursor()
    try:
        for ing, qtd in sorted(ings_qtds):                     # ORDEM POR ID evita deadlock
            baixa_valida(cur, ing, qtd, HOJE, "PEDIDO", U)
        c.commit(); resultado[idx] = "ok"
    except Insuficiente:
        c.rollback(); resultado[idx] = "estoque insuficiente (422)"
    except Exception as e:
        c.rollback(); resultado[idx] = "ERRO " + type(e).__name__ + ": " + str(e).splitlines()[0]
    finally:
        c.close()

def rodada(estoques, a, b):
    c = preparar(); cur = c.cursor()
    for ing, q in estoques.items(): entrada(cur, ing, U, D(q), date(2026, 12, 1))
    c.commit(); c.close()
    res = {}; ts = [threading.Thread(target=pedido, args=(a, res, "A")), threading.Thread(target=pedido, args=(b, res, "B"))]
    [t.start() for t in ts]; [t.join() for t in ts]
    c = ligar(); cur = c.cursor()
    cur.execute("SELECT id, quantidade_estoque FROM ingrediente WHERE id = ANY(%s) ORDER BY id", (list(estoques),))
    saldos = cur.fetchall(); viol = violacoes_invariante(cur); c.close()
    return res, saldos, viol

print("== Teste 1: estoque 50, dois pedidos de 30 ao mesmo tempo (só um pode passar) — 30 rodadas")
cont = {}
for _ in range(30):
    res, saldos, viol = rodada({5: 50}, [(5, D(30))], [(5, D(30))])
    chave = (tuple(sorted(res.values())), str(saldos[0][1]), bool(viol)); cont[chave] = cont.get(chave, 0) + 1
for k, v in cont.items(): print("  ", v, "x", k)
print("== Teste 2: ordem de travamento invertida (A pede 6 e 7; B pede 7 e 6), estoque 100 cada — 30 rodadas")
cont = {}
for _ in range(30):
    res, saldos, viol = rodada({6: 100, 7: 100}, [(6, D(10)), (7, D(10))], [(7, D(10)), (6, D(10))])
    chave = (tuple(sorted(res.values())), tuple(str(s[1]) for s in saldos), bool(viol)); cont[chave] = cont.get(chave, 0) + 1
for k, v in cont.items(): print("  ", v, "x", k)

"""Referência verificada das regras de estoque por linhas (Fase 3 do roteiro): o que o Java deve fazer, em SQL."""
import os
import threading
from datetime import date
from decimal import Decimal as D
import psycopg2

# Ajuste com a variável MF_DSN, ex.: "host=localhost dbname=mente_fria_teste user=postgres password=SUA_SENHA"
DSN = os.environ.get("MF_DSN", "host=localhost dbname=mente_fria_teste user=postgres")
class Insuficiente(Exception): pass

def entrada(cur, ing, usuario, qtd, validade, obs=None):
    cur.execute("""INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, observacao, data_validade, quantidade_restante)
                   VALUES (%s,%s,'ENTRADA',%s,%s,%s,%s) RETURNING id""", (ing, usuario, qtd, obs, validade, qtd))
    cur.execute("UPDATE ingrediente SET quantidade_estoque = quantidade_estoque + %s WHERE id=%s", (qtd, ing))
    return cur.fetchone  # (id não usado)

SQL_LINHAS_VALIDAS = """SELECT id, quantidade_restante FROM movimentacao_estoque
    WHERE ingrediente_id=%s AND tipo='ENTRADA' AND quantidade_restante>0 AND (data_validade IS NULL OR data_validade >= %s)
    ORDER BY data_validade NULLS LAST, id FOR UPDATE"""
SQL_LINHAS_VENCIDAS = """SELECT id, quantidade_restante FROM movimentacao_estoque
    WHERE ingrediente_id=%s AND tipo='ENTRADA' AND quantidade_restante>0 AND data_validade < %s
    ORDER BY data_validade, id FOR UPDATE"""

def _consumir(cur, sql, ing, qtd, hoje, motivo, usuario, obs):
    cur.execute("SELECT 1 FROM ingrediente WHERE id=%s FOR UPDATE", (ing,))      # serializa o ingrediente
    cur.execute(sql, (ing, hoje)); linhas = cur.fetchall()
    if sum(r for _, r in linhas) < qtd:
        raise Insuficiente(f"ingrediente {ing}: pedido {qtd}, disponível {sum(r for _, r in linhas)}")
    falta = qtd
    for lid, rest in linhas:
        if falta <= 0: break
        tira = min(rest, falta)
        cur.execute("UPDATE movimentacao_estoque SET quantidade_restante = quantidade_restante - %s WHERE id=%s", (tira, lid))
        falta -= tira
    cur.execute("UPDATE ingrediente SET quantidade_estoque = quantidade_estoque - %s WHERE id=%s", (qtd, ing))
    cur.execute("""INSERT INTO movimentacao_estoque (ingrediente_id, usuario_id, tipo, quantidade, motivo, observacao)
                   VALUES (%s,%s,'SAIDA',%s,%s,%s)""", (ing, usuario, qtd, motivo, obs))

def baixa_valida(cur, ing, qtd, hoje, motivo, usuario, obs=None):   # PEDIDO ou CONVENCIONAL
    _consumir(cur, SQL_LINHAS_VALIDAS, ing, qtd, hoje, motivo, usuario, obs)
def descarte(cur, ing, qtd, hoje, usuario, obs=None):
    _consumir(cur, SQL_LINHAS_VENCIDAS, ing, qtd, hoje, "DESCARTE_VENCIDO", usuario, obs)

def estado(cur, ing, hoje):
    cur.execute("SELECT quantidade_estoque FROM ingrediente WHERE id=%s", (ing,)); total = cur.fetchone()[0]
    cur.execute("""SELECT COALESCE(SUM(quantidade_restante) FILTER (WHERE data_validade IS NULL OR data_validade >= %s),0),
                          COALESCE(SUM(quantidade_restante) FILTER (WHERE data_validade < %s),0)
                   FROM movimentacao_estoque WHERE ingrediente_id=%s AND tipo='ENTRADA'""", (hoje, hoje, ing))
    v, x = cur.fetchone()
    cur.execute("SELECT data_validade, quantidade_restante FROM movimentacao_estoque WHERE ingrediente_id=%s AND tipo='ENTRADA' ORDER BY data_validade NULLS LAST, id", (ing,))
    return total, v, x, [(str(a) if a else "sem validade", str(b)) for a, b in cur.fetchall()]

def violacoes_invariante(cur):
    cur.execute("""SELECT i.id, i.quantidade_estoque, COALESCE((SELECT SUM(quantidade_restante) FROM movimentacao_estoque m
                   WHERE m.ingrediente_id=i.id AND m.tipo='ENTRADA'),0) FROM ingrediente i
                   WHERE i.quantidade_estoque <> COALESCE((SELECT SUM(quantidade_restante) FROM movimentacao_estoque m
                   WHERE m.ingrediente_id=i.id AND m.tipo='ENTRADA'),0)""")
    return cur.fetchall()

def ligar():
    c = psycopg2.connect(DSN); c.autocommit = False; return c

def preparar():
    c = ligar(); cur = c.cursor()
    cur.execute("TRUNCATE movimentacao_estoque, pedido_produto, pedido, cliente, usuario RESTART IDENTITY CASCADE")
    cur.execute("UPDATE ingrediente SET quantidade_estoque = 0, quantidade_minima = 0")
    cur.execute("INSERT INTO usuario (nome,email,senha_hash,perfil) VALUES ('Dono','d@x','h','ADMINISTRADOR')")
    c.commit(); return c

if __name__ == "__main__":
    HOJE = date(2026, 10, 13); U = 1
    c = preparar(); cur = c.cursor()
    print("== S1 (exemplo 10.1 do roteiro): L1 20 vencida em 12/10, L2 20 vence em 14/10; pedido de 5")
    entrada(cur, 1, U, D(20), date(2026,10,12)); entrada(cur, 1, U, D(20), date(2026,10,14)); c.commit()
    baixa_valida(cur, 1, D(5), HOJE, "PEDIDO", U); c.commit()
    print("   total, válido, vencido, linhas:", estado(cur, 1, HOJE))
    descarte(cur, 1, D(20), HOJE, U); c.commit()
    print("   após descarte de 20 vencidos :", estado(cur, 1, HOJE))
    print("== S2 (contraexemplo da 'simulação'): A 20 vence 20/10; sai 10; entra C 20 vence 15/10; sai 5")
    entrada(cur, 2, U, D(20), date(2026,10,20)); baixa_valida(cur, 2, D(10), HOJE, "CONVENCIONAL", U)
    entrada(cur, 2, U, D(20), date(2026,10,15)); c.commit(); print("   antes da saída de 5:", estado(cur, 2, HOJE)[3])
    baixa_valida(cur, 2, D(5), HOJE, "PEDIDO", U); c.commit(); print("   depois              :", estado(cur, 2, HOJE)[3])
    print("== S3 (baixa que atravessa linhas; sem validade por último)")
    entrada(cur, 3, U, D(3), date(2026,10,14)); entrada(cur, 3, U, D(4), date(2026,10,20)); entrada(cur, 3, U, D(10), None); c.commit()
    baixa_valida(cur, 3, D(6), HOJE, "PEDIDO", U); c.commit(); print("   saída 6:", estado(cur, 3, HOJE)[3])
    baixa_valida(cur, 3, D(5), HOJE, "PEDIDO", U); c.commit(); print("   saída 5:", estado(cur, 3, HOJE)[3])
    print("== S4/S5 (só vencido: pedido recusado; descarte maior que o vencido recusado)")
    entrada(cur, 4, U, D(8), date(2026,10,10)); c.commit()
    for nome, f in [("pedido de 1", lambda: baixa_valida(cur, 4, D(1), HOJE, "PEDIDO", U)), ("descarte de 9", lambda: descarte(cur, 4, D(9), HOJE, U))]:
        try: f(); c.commit(); print("   ", nome, "ACEITO (erro!)")
        except Insuficiente as e: c.rollback(); print("   ", nome, "-> 422:", e, "| estado:", estado(cur, 4, HOJE)[:3])
    descarte(cur, 4, D(8), HOJE, U); c.commit(); print("    descarte de 8 -> ", estado(cur, 4, HOJE)[:3])
    print("== invariante (quantidade_estoque = soma dos restantes): violações =", violacoes_invariante(cur))
    cur.execute("SELECT motivo, COUNT(*) FROM movimentacao_estoque WHERE tipo='SAIDA' GROUP BY motivo ORDER BY motivo"); print("   saídas por motivo:", cur.fetchall())
    c.close()

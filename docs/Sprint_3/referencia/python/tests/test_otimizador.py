import json
import random
import subprocess
import sys
from decimal import Decimal
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).parent.parent / "app"))
sys.path.insert(0, str(Path(__file__).parent))
import otimizador                      # noqa: E402
from oraculo import oraculo            # noqa: E402

SCRIPT = Path(__file__).parent.parent / "app" / "otimizador.py"


def executar(payload):
    """Roda o script como o Java fará: JSON no stdin, JSON no stdout."""
    return subprocess.run([sys.executable, str(SCRIPT)], input=json.dumps(payload, ensure_ascii=False),
                          capture_output=True, text=True, encoding="utf-8", timeout=30)


def resolver(payload):                 # mesmo caminho do main(): JSON -> Decimal -> processar
    return otimizador.processar(json.loads(json.dumps(payload), parse_float=Decimal))


def ing(i, estoque, custo):
    return {"id": i, "quantidade_estoque": estoque, "custo_unitario": custo}


def prod(i, preco, *comp):
    return {"id": i, "preco_venda": preco,
            "composicao": [{"ingrediente_id": a, "quantidade_utilizada": q} for a, q in comp]}


def exemplo_contrato(granola=0):
    return {
        "ingredientes": [ing(1, 40.0, 18.50), ing(2, 2.0, 12.00), ing(3, granola, 22.00),
                         ing(4, 12.0, 5.50), ing(7, 300.0, 0.35)],
        "produtos": [prod(1, 12.90, (1, 0.300), (2, 0.030), (7, 1.0)),
                     prod(2, 18.90, (1, 0.450), (3, 0.040), (4, 0.050), (7, 1.0))],
    }


# ---- cenários oficiais ------------------------------------------------------------------
def test_exemplo_do_contrato_via_subprocesso():
    r = executar(exemplo_contrato())
    assert r.returncode == 0, r.stderr
    assert json.loads(r.stdout) == {
        "status": "sucesso", "receita_estimada": 851.40, "margem_estimada": 438.24,
        "recomendacao": [{"produto_id": 1, "quantidade": 66}, {"produto_id": 2, "quantidade": 0}],
        "consumo": [{"ingrediente_id": 1, "quantidade": 19.8}, {"ingrediente_id": 2, "quantidade": 1.98},
                    {"ingrediente_id": 7, "quantidade": 66.0}]}


def test_stdout_contem_somente_o_json_em_utf8():
    r = executar(exemplo_contrato())
    assert r.stdout.strip().startswith("{") and r.stdout.strip().endswith("}")
    json.loads(r.stdout)               # se houver banner de licença, isto falha


def test_exemplo_didatico_nao_e_o_arredondamento_do_simplex():
    saida = resolver(exemplo_contrato(granola=12.0))
    plano = {i["produto_id"]: i["quantidade"] for i in saida["recomendacao"]}
    assert plano == {1: 64, 2: 46}
    assert saida["margem_estimada"] == 842.18 and saida["receita_estimada"] == 1695.00
    assert plano != {1: 66, 2: 44}     # arredondar para baixo daria 837,32 (R$ 4,86 pior)


def test_maior_receita_nao_e_maior_lucro():
    # Os dois produtos disputam 10 kg do ingrediente 1. O produto 2 tem preço maior (receita 160 se
    # só ele for feito), mas o ingrediente 2 o deixa com lucro menor (70). O produto 1 rende 90.
    e = {"ingredientes": [ing(1, 10.0, 1.00), ing(2, 10.0, 8.00)],
         "produtos": [prod(1, 10.00, (1, 1.0)), prod(2, 16.00, (2, 1.0), (1, 1.0))]}
    saida = resolver(e)
    plano = {i["produto_id"]: i["quantidade"] for i in saida["recomendacao"]}
    assert plano == {1: 10, 2: 0}                    # o plano de maior receita seria {1: 0, 2: 10}
    assert saida["margem_estimada"] == 90.0 and saida["receita_estimada"] == 100.0
    esperado = oraculo(e)
    assert plano == esperado["plano"] and esperado["margem"] == Decimal("90")


# ---- regras do modelo -------------------------------------------------------------------
def test_produto_com_lucro_zero_ou_negativo_recebe_zero():
    e = {"ingredientes": [ing(1, 10.0, 5.00)],
         "produtos": [prod(1, 5.00, (1, 1.0)), prod(2, 4.00, (1, 1.0))]}   # lucro 0 e -1
    saida = resolver(e)
    assert [i["quantidade"] for i in saida["recomendacao"]] == [0, 0]
    assert saida["margem_estimada"] == 0


def test_desempate_e_deterministico_e_favorece_menor_id():
    e = {"ingredientes": [ing(1, 10.0, 0.00)],
         "produtos": [prod(1, 5.00, (1, 1.0)), prod(2, 5.00, (1, 1.0))]}   # mesmo lucro por unidade
    a, b = resolver(e), resolver(e)
    assert a == b
    assert [i["quantidade"] for i in a["recomendacao"]] == [10, 0]


def test_estoque_zerado_retorna_sucesso_com_zeros():
    e = {"ingredientes": [ing(1, 0.0, 1.00)], "produtos": [prod(1, 10.00, (1, 1.0))]}
    saida = resolver(e)
    assert saida["status"] == "sucesso" and saida["margem_estimada"] == 0
    assert saida["recomendacao"] == [{"produto_id": 1, "quantidade": 0}]


# ---- erros (status "erro", exit code 0) -------------------------------------------------
@pytest.mark.parametrize("mutacao", [
    lambda e: e["ingredientes"][0].pop("custo_unitario"),                     # sem custo
    lambda e: e["ingredientes"][0].update(custo_unitario=-1),                 # custo negativo
    lambda e: e["ingredientes"][0].update(quantidade_estoque=-1),             # estoque negativo
    lambda e: e["produtos"][0].update(preco_venda=0),                         # preço <= 0
    lambda e: e["produtos"][0]["composicao"][0].update(quantidade_utilizada=0),
    lambda e: e["produtos"][0]["composicao"][0].update(quantidade_utilizada=0.0001),   # 4 casas
    lambda e: e["produtos"][0].update(preco_venda="12,90"),                   # texto
    lambda e: e["produtos"][0]["composicao"][0].update(ingrediente_id=999),   # ingrediente ausente
    lambda e: e["produtos"][0].update(composicao=[]),                         # sem composição
    lambda e: e.update(produtos=[]),                                          # lista vazia
])
def test_entradas_invalidas_retornam_erro_tratado(mutacao):
    e = exemplo_contrato()
    mutacao(e)
    r = executar(e)
    assert r.returncode == 0, r.stderr
    assert json.loads(r.stdout)["status"] == "erro"


def test_json_invalido_retorna_erro_tratado():
    r = subprocess.run([sys.executable, str(SCRIPT)], input="isto não é json",
                       capture_output=True, text=True, encoding="utf-8", timeout=30)
    assert r.returncode == 0 and json.loads(r.stdout)["status"] == "erro"


def test_entrada_com_bom_e_aceita():
    # O pipe do PowerShell (Get-Content ... | python ...) envia um BOM UTF-8 antes do JSON.
    r = subprocess.run([sys.executable, str(SCRIPT)], input="﻿" + json.dumps({"echo": "Açaí (polpa)"}, ensure_ascii=False),
                       capture_output=True, text=True, encoding="utf-8", timeout=30)
    assert r.returncode == 0, r.stderr
    assert json.loads(r.stdout) == {"status": "sucesso", "echo": "AÇAÍ (POLPA)"}


# ---- propriedade: coincide com o oráculo em instâncias aleatórias (semente fixa) --------
def _instancia_aleatoria(rng):
    n_ing, n_prod = rng.randint(2, 4), rng.randint(2, 3)
    ings = [ing(i, round(rng.uniform(0.5, 6), 3), round(rng.uniform(0.1, 20), 2)) for i in range(1, n_ing + 1)]
    prods = []
    for p in range(1, n_prod + 1):
        usados = rng.sample(range(1, n_ing + 1), rng.randint(1, n_ing))
        comp = [(i, round(rng.uniform(0.1, 1.5), 3)) for i in usados]
        prods.append(prod(p, round(rng.uniform(3, 40), 2), *comp))
    return {"ingredientes": ings, "produtos": prods}


def test_coincide_com_o_oraculo_em_200_instancias():
    rng = random.Random(20261012)
    comparadas = 0
    for _ in range(200):
        e = _instancia_aleatoria(rng)
        try:
            esperado = oraculo(e)
        except ValueError:
            continue
        saida = resolver(e)
        plano = {i["produto_id"]: i["quantidade"] for i in saida["recomendacao"]}
        assert plano == esperado["plano"], e
        assert Decimal(str(saida["margem_estimada"])) == esperado["margem"].quantize(Decimal("0.01"), "ROUND_HALF_UP"), e
        comparadas += 1
    assert comparadas >= 150


# ---- modo eco (Hello World de infraestrutura; Contrato §1.5) -------------------------------
def test_eco_com_acentos_via_subprocesso():
    r = executar({"echo": "Açaí (polpa)"})
    assert r.returncode == 0, r.stderr
    assert json.loads(r.stdout) == {"status": "sucesso", "echo": "AÇAÍ (POLPA)"}


@pytest.mark.parametrize("entrada", [{"echo": 123}, {"echo": None}, {"echo": ["a"]}])
def test_eco_invalido_retorna_erro_tratado(entrada):
    r = executar(entrada)
    assert r.returncode == 0 and json.loads(r.stdout) == {"status": "erro", "mensagem": "Campo 'echo' ausente ou inválido"}


def test_consumo_nunca_passa_do_estoque_e_so_lista_o_que_consome():
    saida = resolver(exemplo_contrato(granola=12.0))                    # plano 64/46
    consumo = {c["ingrediente_id"]: Decimal(str(c["quantidade"])) for c in saida["consumo"]}
    assert consumo == {1: Decimal("39.900"), 2: Decimal("1.920"), 3: Decimal("1.840"), 4: Decimal("2.300"), 7: Decimal("110.000")}
    estoque = {1: 40, 2: 2, 3: 12, 4: 12, 7: 300}
    assert all(consumo[i] <= estoque[i] for i in consumo)

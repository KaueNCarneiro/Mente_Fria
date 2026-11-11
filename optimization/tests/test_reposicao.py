"""Testes do módulo de reposição (US14). Os valores esperados foram calculados à mão (exemplo didático)
ou pelo oráculo de força bruta, nunca pelo próprio módulo (Plano de Testes, seção 1)."""
import json
import random
import subprocess
import sys
from decimal import Decimal
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).parent.parent / "app"))
sys.path.insert(0, str(Path(__file__).parent))
import reposicao                                 # noqa: E402
from oraculo_reposicao import oraculo_reposicao  # noqa: E402

SCRIPT = Path(__file__).parent.parent / "app" / "reposicao.py"
EXEMPLOS = Path(__file__).parent.parent / "exemplos"


def executar(payload, texto=None):
    """Roda o script como o Java fará: JSON no stdin, JSON no stdout."""
    entrada = texto if texto is not None else json.dumps(payload, ensure_ascii=False)
    return subprocess.run([sys.executable, str(SCRIPT)], input=entrada, capture_output=True,
                          text=True, encoding="utf-8", timeout=30)


def resolver(payload):                           # mesmo caminho do main(): JSON -> Decimal -> processar
    return reposicao.processar(json.loads(json.dumps(payload), parse_float=Decimal))


def ing(i, consumo, estoque, minimo, custo):
    return {"id": i, "consumo_periodo": consumo, "quantidade_estoque": estoque,
            "quantidade_minima": minimo, "custo_unitario": custo}


def exemplo_didatico(orcamento):
    # açaí: min 0,20 nec 1,00 | granola: min 0,10 nec 0,40 | leite condensado: min 0 nec 0,20
    return {"orcamento": orcamento, "ingredientes": [
        ing(1, 0.800, 0.300, 0.500, 18.50), ing(3, 0.300, 0.000, 0.100, 22.00), ing(2, 0.200, 0.100, 0.100, 12.00)]}


def linha(i, sugerida, minima, necessaria, custo):
    return {"ingrediente_id": i, "quantidade_sugerida": sugerida, "quantidade_minima_compra": minima,
            "quantidade_necessaria": necessaria, "custo_estimado": custo}


# ---- exemplo didático (conta à mão no tutorial do Kauê, §11) ----------------------------
def test_exemplo_didatico_via_subprocesso():
    r = executar(exemplo_didatico(20.00))
    assert r.returncode == 0, r.stderr
    assert json.loads(r.stdout) == {
        "status": "sucesso", "orcamento_suficiente": True, "custo_total": 19.95, "valor_faltante": 0.0,
        "cobertura_minima": 0.66,
        "itens": [linha(1, 0.66, 0.2, 1.0, 12.21), linha(3, 0.27, 0.1, 0.4, 5.94), linha(2, 0.15, 0.0, 0.2, 1.8)]}


def test_orcamento_que_cobre_toda_a_necessidade():
    r = resolver(exemplo_didatico(50.00))
    assert r["orcamento_suficiente"] is True and r["cobertura_minima"] == 1.0
    assert r["custo_total"] == 29.70
    assert [l["quantidade_sugerida"] for l in r["itens"]] == [1.0, 0.4, 0.2]


def test_orcamento_insuficiente_devolve_os_minimos_e_quanto_falta():
    r = resolver(exemplo_didatico(5.00))
    assert r == {"status": "sucesso", "orcamento_suficiente": False, "custo_total": 5.90, "valor_faltante": 0.90,
                 "cobertura_minima": None,
                 "itens": [linha(1, 0.2, 0.2, 1.0, 3.70), linha(3, 0.1, 0.1, 0.4, 2.20), linha(2, 0.0, 0.0, 0.2, 0.0)]}


def test_orcamento_exatamente_igual_aos_minimos_e_suficiente():
    r = resolver(exemplo_didatico(5.90))
    assert r["orcamento_suficiente"] is True and r["custo_total"] == 5.90
    assert r["cobertura_minima"] == 0.0           # o leite condensado (mínimo 0) fica sem compra


def test_orcamento_zero_sem_necessidade_e_suficiente():
    r = resolver({"orcamento": 0, "ingredientes": [ing(1, 0.500, 50.000, 10.000, 18.50)]})
    assert r["orcamento_suficiente"] is True and r["custo_total"] == 0.0 and r["cobertura_minima"] == 1.0
    assert r["itens"] == [linha(1, 0.0, 0.0, 0.0, 0.0)]


def test_quantidades_sao_arredondadas_para_cima_em_centesimos():
    r = resolver({"orcamento": 100, "ingredientes": [ing(1, 0.001, 0.000, 0.001, 1.00)]})
    assert r["itens"] == [linha(1, 0.01, 0.01, 0.01, 0.01)]     # 0,001 e 0,002 viram 0,01 (nunca abaixo)


def test_desempate_favorece_o_ingrediente_mais_consumido():
    # Mesma necessidade (0,10) e mesmo custo; sobra 0,01 de orçamento para um centésimo a mais.
    entrada = {"orcamento": 0.15, "ingredientes": [ing(2, 0.050, 0.000, 0.050, 1.00), ing(1, 0.100, 0.000, 0.000, 1.00)]}
    r = resolver(entrada)
    sug = {l["ingrediente_id"]: l["quantidade_sugerida"] for l in r["itens"]}
    assert sug == {1: 0.08, 2: 0.07} and r["cobertura_minima"] == 0.7
    assert oraculo_reposicao(entrada)["plano"] == {2: 7, 1: 8}


def test_tabela_ordenada_por_custo_estimado_decrescente():
    r = resolver(exemplo_didatico(20.00))
    custos = [l["custo_estimado"] for l in r["itens"]]
    assert custos == sorted(custos, reverse=True)


# ---- conferência contra o oráculo (força bruta, sem solver) -------------------------------
def _instancia_aleatoria(rng):
    ingredientes = [ing(i, rng.randint(0, 120) / 1000, rng.randint(0, 100) / 1000, rng.randint(0, 60) / 1000,
                        rng.randint(1, 3000) / 100) for i in rng.sample(range(1, 30), rng.randint(1, 3))]
    total = sum(Decimal(str(i["custo_unitario"])) * Decimal(str(i["consumo_periodo"] + i["quantidade_minima"]))
                for i in ingredientes)
    orcamento = float((total * Decimal(rng.choice([0, 0.1, 0.3, 0.5, 0.7, 0.9, 1.2]))).quantize(Decimal("0.01")))
    return {"orcamento": orcamento, "ingredientes": ingredientes}


def test_coincide_com_o_oraculo_em_150_instancias():
    rng = random.Random(20261026)
    for _ in range(150):
        entrada = _instancia_aleatoria(rng)
        esperado = oraculo_reposicao(entrada)
        r = resolver(entrada)
        assert r["orcamento_suficiente"] == esperado["suficiente"], entrada
        plano = {l["ingrediente_id"]: round(l["quantidade_sugerida"] * 100) for l in r["itens"]}
        assert plano == esperado["plano"], entrada
        assert Decimal(str(r["custo_total"])) == esperado["gasto"].quantize(Decimal("0.01")), entrada


# ---- dados de demonstração (seed_demo.sql, período de 60 dias) -----------------------------
def test_exemplo_demo_bate_com_a_saida_guardada():
    entrada = json.loads((EXEMPLOS / "entrada_reposicao_demo.json").read_text(encoding="utf-8"))
    esperado = json.loads((EXEMPLOS / "saida_reposicao_demo.json").read_text(encoding="utf-8"))
    r = executar(entrada)
    assert r.returncode == 0, r.stderr
    assert json.loads(r.stdout) == esperado


# ---- erros tratados e formato ---------------------------------------------------------------
@pytest.mark.parametrize("mutacao", [
    lambda e: e.pop("orcamento"),
    lambda e: e.update(orcamento=-1),
    lambda e: e.update(orcamento="100"),
    lambda e: e.update(orcamento=10.001),
    lambda e: e.update(ingredientes=[]),
    lambda e: e["ingredientes"].append(dict(e["ingredientes"][0])),          # id repetido
    lambda e: e["ingredientes"][0].update(custo_unitario=18.505),
    lambda e: e["ingredientes"][0].update(consumo_periodo=-0.1),
    lambda e: e["ingredientes"][0].update(quantidade_minima=True),
    lambda e: e["ingredientes"][0].pop("quantidade_estoque"),
])
def test_entradas_invalidas_retornam_erro_tratado(mutacao):
    entrada = exemplo_didatico(20.00)
    mutacao(entrada)
    r = executar(entrada)
    assert r.returncode == 0, r.stderr
    saida = json.loads(r.stdout)
    assert saida["status"] == "erro" and saida["mensagem"]


def test_json_invalido_e_entrada_com_bom():
    r = executar(None, texto="{nao e json")
    assert r.returncode == 0 and json.loads(r.stdout) == {"status": "erro", "mensagem": "JSON de entrada inválido"}
    r = executar(None, texto="﻿" + json.dumps(exemplo_didatico(20.00)))
    assert r.returncode == 0, r.stderr
    assert json.loads(r.stdout)["custo_total"] == 19.95


def test_stdout_contem_somente_o_json():
    r = executar(exemplo_didatico(20.00))
    assert r.stdout.strip().startswith("{") and r.stdout.strip().endswith("}")
    json.loads(r.stdout)

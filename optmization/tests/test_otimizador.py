import json
import subprocess
import sys
from pathlib import Path

SCRIPT = Path(__file__).parent.parent / "app" / "otimizador.py"


def executar(payload: dict):
    """Roda o script exatamente como o Java fará: JSON no stdin, JSON no stdout."""
    return subprocess.run(
        [sys.executable, str(SCRIPT)],
        input=json.dumps(payload, ensure_ascii=False),
        capture_output=True,
        text=True,
        encoding="utf-8",
        timeout=10,
    )


# --- Exemplo oficial do Contrato §1.1/1.2 ---

def test_exemplo_do_contrato():
    payload = {
        "ingredientes": [
            {"id": 1, "quantidade_estoque": 40.000},
            {"id": 2, "quantidade_estoque": 2.000},
            {"id": 3, "quantidade_estoque": 0.000},
            {"id": 4, "quantidade_estoque": 12.000},
            {"id": 7, "quantidade_estoque": 300.000},
        ],
        "produtos": [
            {
                "id": 1,
                "preco_venda": 12.90,
                "composicao": [
                    {"ingrediente_id": 1, "quantidade_utilizada": 0.300},
                    {"ingrediente_id": 2, "quantidade_utilizada": 0.030},
                    {"ingrediente_id": 7, "quantidade_utilizada": 1.000},
                ],
            },
            {
                "id": 2,
                "preco_venda": 18.90,
                "composicao": [
                    {"ingrediente_id": 1, "quantidade_utilizada": 0.450},
                    {"ingrediente_id": 3, "quantidade_utilizada": 0.040},
                    {"ingrediente_id": 4, "quantidade_utilizada": 0.050},
                    {"ingrediente_id": 7, "quantidade_utilizada": 1.000},
                ],
            },
        ],
    }
    resultado = executar(payload)
    assert resultado.returncode == 0, resultado.stderr
    saida = json.loads(resultado.stdout)
    assert saida == {
        "status": "sucesso",
        "receita_estimada": 851.40,
        "recomendacao": [
            {"produto_id": 1, "quantidade": 66},
            {"produto_id": 2, "quantidade": 0},
        ],
    }


# --- CT23: um único ingrediente escasso limita um único produto ---

def test_ingrediente_escasso_limita_produto_unico():
    payload = {
        "ingredientes": [{"id": 1, "quantidade_estoque": 1.200}],
        "produtos": [
            {"id": 1, "preco_venda": 10.00, "composicao": [
                {"ingrediente_id": 1, "quantidade_utilizada": 0.400}
            ]},
        ],
    }
    saida = json.loads(executar(payload).stdout)
    assert saida["recomendacao"] == [{"produto_id": 1, "quantidade": 3}]
    assert saida["receita_estimada"] == 30.00


# --- CT24: dois produtos disputam o mesmo ingrediente escasso ---

def test_dois_produtos_disputam_mesmo_ingrediente():
    payload = {
        "ingredientes": [{"id": 1, "quantidade_estoque": 10.000}],
        "produtos": [
            {"id": 1, "preco_venda": 5.00, "composicao": [
                {"ingrediente_id": 1, "quantidade_utilizada": 1.000}
            ]},
            {"id": 2, "preco_venda": 8.00, "composicao": [
                {"ingrediente_id": 1, "quantidade_utilizada": 2.000}
            ]},
        ],
    }
    saida = json.loads(executar(payload).stdout)
    consumo_total = sum(
        item["quantidade"] * (1.000 if item["produto_id"] == 1 else 2.000)
        for item in saida["recomendacao"]
    )
    assert consumo_total <= 10.000
    # produto 1 rende 5/unidade de ingrediente; produto 2 rende 4/unidade — produto 1 vence
    assert saida["recomendacao"] == [
        {"produto_id": 1, "quantidade": 10},
        {"produto_id": 2, "quantidade": 0},
    ]


# --- §1.2: estoque insuficiente para produzir qualquer coisa ainda é sucesso ---

def test_estoque_zerado_retorna_sucesso_com_zeros():
    payload = {
        "ingredientes": [{"id": 1, "quantidade_estoque": 0.000}],
        "produtos": [
            {"id": 1, "preco_venda": 10.00, "composicao": [
                {"ingrediente_id": 1, "quantidade_utilizada": 1.000}
            ]},
        ],
    }
    saida = json.loads(executar(payload).stdout)
    assert saida["status"] == "sucesso"
    assert saida["recomendacao"] == [{"produto_id": 1, "quantidade": 0}]
    assert saida["receita_estimada"] == 0


# --- §1.3: casos de erro ---

def test_lista_de_produtos_vazia():
    saida = json.loads(executar({"ingredientes": [], "produtos": []}).stdout)
    assert saida["status"] == "erro"


def test_produto_sem_composicao():
    payload = {"ingredientes": [], "produtos": [{"id": 1, "preco_venda": 10.00, "composicao": []}]}
    saida = json.loads(executar(payload).stdout)
    assert saida["status"] == "erro"


def test_composicao_cita_ingrediente_ausente():
    payload = {
        "ingredientes": [{"id": 1, "quantidade_estoque": 5.000}],
        "produtos": [{"id": 1, "preco_venda": 10.00, "composicao": [
            {"ingrediente_id": 999, "quantidade_utilizada": 1.000}
        ]}],
    }
    saida = json.loads(executar(payload).stdout)
    assert saida["status"] == "erro"


def test_numero_invalido():
    payload = {
        "ingredientes": [{"id": 1, "quantidade_estoque": 5.000}],
        "produtos": [{"id": 1, "preco_venda": "não é número", "composicao": [
            {"ingrediente_id": 1, "quantidade_utilizada": 1.000}
        ]}],
    }
    saida = json.loads(executar(payload).stdout)
    assert saida["status"] == "erro"
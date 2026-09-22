import json
import subprocess
import sys
from pathlib import Path

SCRIPT = Path(__file__).parent.parent / "app" / "otimizador.py"


def executar(texto_entrada: str):
    """Roda o script como o Java fará: JSON no stdin, JSON no stdout."""
    return subprocess.run(
        [sys.executable, str(SCRIPT)],
        input=texto_entrada,
        capture_output=True,
        text=True,
        encoding="utf-8",
        timeout=10,
    )


def test_eco_com_acentos():
    resultado = executar(json.dumps({"echo": "Açaí (polpa)"}, ensure_ascii=False))
    # contrato: código 0 significa que stdout tem JSON válido (mesmo que status seja "erro")
    assert resultado.returncode == 0, resultado.stderr
    assert resultado.stderr == ""  # não deve haver logs no stderr quando stdout contém o JSON
    assert json.loads(resultado.stdout) == {"status": "sucesso", "echo": "AÇAÍ (POLPA)"}


def test_campo_ausente_retorna_erro_tratado():
    resultado = executar(json.dumps({"outro": 1}))
    assert resultado.returncode == 0
    assert resultado.stderr == ""
    assert json.loads(resultado.stdout)["status"] == "erro"


def test_json_invalido_retorna_erro_tratado():
    resultado = executar("isto não é json")
    assert resultado.returncode == 0
    assert resultado.stderr == ""
    assert json.loads(resultado.stdout)["status"] == "erro"
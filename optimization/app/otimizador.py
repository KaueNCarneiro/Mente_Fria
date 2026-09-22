
"""
lê um JSON na entrada padrão (stdin) e escreve um JSON na saída padrão (stdout).
Não acessa banco nem rede.
Código de saída 0 = há um JSON válido no stdout (inclusive com status "erro").
Código diferente de 0 = falha inesperada (exceção não tratada, processo morto).
"""
import json
import sys
from typing import Any, Dict


def processar(entrada: Dict[str, Any]) -> Dict[str, Any]:
    texto = entrada.get("echo")
    if not isinstance(texto, str):
        return {"status": "erro", "mensagem": "Campo 'echo' ausente ou inválido"}
    return {"status": "sucesso", "echo": texto.upper()}


def _imprimir_json_e_flush(obj: Dict[str, Any]) -> None:
    """Escreve exatamente um JSON no stdout (sem outros prints) e força flush."""
    sys.stdout.write(json.dumps(obj, ensure_ascii=False))
    sys.stdout.write("\n")
    sys.stdout.flush()


def main() -> int:
    # Garante UTF-8 na entrada e na saída, independentemente do sistema (Windows usa outro padrão)
    # (contrato §1 exige isso)
    try:
        sys.stdin.reconfigure(encoding="utf-8")
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        # Reconfigure pode não existir em runtimes muito antigos; se falhar, deixamos a runtime usar o padrão.
        pass

    # 1) ler JSON de entrada: se inválido, devolver JSON de erro e exit code 0
    try:
        entrada = json.load(sys.stdin)
    except json.JSONDecodeError:
        _imprimir_json_e_flush({"status": "erro", "mensagem": "JSON de entrada inválido"})
        return 0

    # 2) processar: erros previsíveis de dados -> status "erro" no stdout e exit code 0
    try:
        resposta = processar(entrada)
    except (ValueError, TypeError, KeyError) as e:
        # Erro de conteúdo/dados — o contrato define 'status: "erro"' para esses casos.
        _imprimir_json_e_flush({"status": "erro", "mensagem": str(e)})
        return 0

    # 3) saída normal (sucesso): um único JSON no stdout
    _imprimir_json_e_flush(resposta)
    return 0


if __name__ == "__main__":
    sys.exit(main())
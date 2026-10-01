import json
import sys

import gurobipy as gp
from gurobipy import GRB


def processar(entrada: dict) -> dict:
    produtos = entrada.get("produtos")
    ingredientes = entrada.get("ingredientes")

    # --- Validações (§1.3) ---
    if not isinstance(produtos, list) or len(produtos) == 0:
        return {"status": "erro", "mensagem": "Nenhum produto ativo para otimização"}
    if not isinstance(ingredientes, list):
        return {"status": "erro", "mensagem": "Lista de ingredientes ausente ou inválida"}

    estoque_por_id = {}
    for ing in ingredientes:
        try:
            estoque_por_id[ing["id"]] = float(ing["quantidade_estoque"])
        except (KeyError, TypeError, ValueError):
            return {"status": "erro", "mensagem": "Números inválidos em ingredientes"}

    for produto in produtos:
        composicao = produto.get("composicao")
        if not isinstance(composicao, list) or len(composicao) == 0:
            return {"status": "erro", "mensagem": f"Produto {produto.get('id')} sem composição"}
        for item in composicao:
            if item.get("ingrediente_id") not in estoque_por_id:
                return {"status": "erro", "mensagem": "Composição cita ingrediente ausente da lista"}
            try:
                float(item["quantidade_utilizada"])
            except (KeyError, TypeError, ValueError):
                return {"status": "erro", "mensagem": "Números inválidos em composição"}
        try:
            float(produto["preco_venda"])
        except (KeyError, TypeError, ValueError):
            return {"status": "erro", "mensagem": "Números inválidos em produtos"}

    # --- Modelo ---
    with gp.Env(empty=True) as env:
        env.setParam("OutputFlag", 0)
        env.start()
        with gp.Model(env=env, name="mente_fria_producao") as modelo:
            quantidade_var = {
                p["id"]: modelo.addVar(vtype=GRB.INTEGER, lb=0, name=f"qtd_{p['id']}")
                for p in produtos
            }

            modelo.setObjective(
                gp.quicksum(float(p["preco_venda"]) * quantidade_var[p["id"]] for p in produtos),
                GRB.MAXIMIZE,
            )

            for ingrediente_id, estoque in estoque_por_id.items():
                consumo = gp.quicksum(
                    float(item["quantidade_utilizada"]) * quantidade_var[p["id"]]
                    for p in produtos
                    for item in p["composicao"]
                    if item["ingrediente_id"] == ingrediente_id
                )
                modelo.addConstr(consumo <= estoque, name=f"estoque_{ingrediente_id}")

            modelo.optimize()

            if modelo.Status != GRB.OPTIMAL:
                return {"status": "erro", "mensagem": "Não foi possível calcular uma solução"}

            recomendacao = [
                {"produto_id": p["id"], "quantidade": int(round(quantidade_var[p["id"]].X))}
                for p in produtos
            ]
            return {
                "status": "sucesso",
                "receita_estimada": round(modelo.ObjVal, 2),
                "recomendacao": recomendacao,
            }


def main() -> int:
    sys.stdin.reconfigure(encoding="utf-8")
    sys.stdout.reconfigure(encoding="utf-8")
    try:
        entrada = json.load(sys.stdin)
    except json.JSONDecodeError:
        print(json.dumps({"status": "erro", "mensagem": "JSON de entrada inválido"}, ensure_ascii=False))
        return 0
    print(json.dumps(processar(entrada), ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
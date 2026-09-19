# Contexto do projeto Mente Fria

Consolidação em 18/09/2026 dos documentos fornecidos pela equipe. Este registro orienta o desenvolvimento; não substitui os artefatos oficiais nem aprova decisões ainda abertas.

## Fontes

- `Documento de Visão — [Mente Fria].pdf`: problema, objetivos, limites e requisitos acadêmicos.
- `Backlog_Priorizado_Mente_Fria.docx`: histórias US1–US26, critérios e prioridades.
- `Termo_de_Aceite_Mente_Fria.docx`: escopo aceito, critérios de pronto, stack e responsabilidades.
- `Diagramas_UML.md`: casos de uso, classes e rastreabilidade.
- `DER.md`: modelo e dicionário de dados.
- `Script_DDL.sql`: estrutura PostgreSQL e dados de exemplo.
- `Roteiro do Protótipo Navegável — Mente Fria (1).md`: fluxos e telas demonstrativos.
- `Plano_de_Testes_Mente_Fria (1).md`: estratégia, cenários e critérios de bloqueio de merge.
- `README_7708.md`: referência anterior de setup, com campos ainda não definidos.
- `INSTRUCOES_PRINCIPAIS.md`: cópia das orientações fornecidas e explicitamente adotadas pelo usuário.

## Escopo documentado

Administrador e Funcionário têm permissões distintas. Ambos registram movimentações e pedidos, cadastram clientes e consultam alertas de estoque e validade. Cadastros administrativos, preços, recomendações e dashboard são funções do Administrador.

O fluxo principal é cadastro de ingredientes e produtos, movimentação de estoque, recomendação de produção, registro de pedidos e baixa automática dos ingredientes. Pedidos devem preservar o preço praticado e ser rejeitados integralmente quando faltar estoque.

A otimização maximiza receita usando preços, composições e disponibilidade compartilhada dos ingredientes. Recomendações de produção, reposição e marketing são calculadas sob demanda, sem histórico persistido. O modelo matemático detalhado ainda precisa ser definido.

O backlog classifica clientes (US13), exportação CSV (US19) e porção padrão por unidade (US26) como Could. A US26 já consta no DOCX, com estimativa P e Sprint 3. A tabela de unidades participa do modelo atual por uma FK obrigatória; isso não torna automaticamente a tela de gestão de padrões uma entrega Must.

Ficam fora do semestre histórico de preços, compras automáticas de fornecedores, financeiro completo, publicação automática de campanhas e integração direta com delivery (US21–US25).

## Orientação de arquitetura

As pastas existentes refletem a separação documentada entre interface, backend, banco e otimização. O backend deve proteger permissões, validar entradas e coordenar a gravação transacional de pedidos e estoque. A interface apresenta informações e validações de uso; o módulo Python concentra a modelagem matemática.

Essa separação não define o mecanismo de chamada Java–Python, contratos de API, autenticação ou ferramenta de build. Essas decisões permanecem abertas.

## Pontos a resolver antes da implementação correspondente

| Ponto | Evidência e impacto | Encaminhamento |
|---|---|---|
| Consumo por validade | O DER prevê FEFO e entradas ainda não totalmente consumidas, mas o DDL não registra vínculo entre saídas e entradas nem saldo por entrada. O algoritmo de reconstrução também não está definido. Alertas podem incluir entradas já consumidas se consultarem apenas datas. | Antes de estoque/validade, comparar reconstrução determinística do histórico com registro explícito das alocações de consumo; validar a opção e o tratamento de entradas sem validade e ingredientes vencidos. |
| Identificação do açaí | US5 e CT15 exigem especificamente açaí; o modelo não possui identificação estável do ingrediente-base. | Definir com a equipe como identificá-lo antes de implementar composição; evitar depender silenciosamente de um nome editável. |
| Integração e ambiente | README mantém versões, build, licença e comunicação Java–Python em aberto. | Apresentar opções e validar antes de configurar os módulos dependentes. |
| Modelo de otimização | Receita é o objetivo documentado; domínio inteiro ou contínuo das quantidades, limites de demanda e tratamento de estoque vencido não estão formalizados. | Especificar variáveis, objetivo e restrições antes do solver, sem trocar receita por lucro silenciosamente. |
| Reposição e marketing | Período de consumo, fórmula de reposição e critério de prioridade das campanhas precisam de detalhamento. | Formalizar regras e exemplos com resultados esperados antes de implementar essas recomendações. |
| Dados de exemplo | No DDL, o pedido 2 consome 0,600 + 0,300 = 0,900 de açaí pela composição, mas a saída correspondente registra 1,050. O saldo 40,000 também não corresponde a 50,000 menos as saídas registradas, 3,450. | Reconciliar o seed antes de utilizá-lo como base para testes de estoque; não tratar o conjunto atual como evidência de consistência. |

Esses pontos não impedem a organização documental. Nenhuma alteração de modelo de dados ou regra de negócio foi aprovada por este registro.

## Sequência sugerida

1. Definir ambiente, build, autenticação e contratos necessários ao primeiro incremento.
2. Implementar base executável, conexão PostgreSQL e cadastro/autenticação com permissões.
3. Implementar ingredientes e produtos, resolvendo identificação do açaí e dependência de unidades.
4. Implementar estoque e pedidos com atomicidade, controle de concorrência e regras de validade definidas.
5. Integrar otimização com casos pequenos e resultados esperados independentes do solver.
6. Entregar recomendações, dashboard e alertas conforme prioridades; executar aceite e publicar.

Cada incremento deve incluir verificação adequada, documentação do comportamento real e registro das decisões aprovadas.

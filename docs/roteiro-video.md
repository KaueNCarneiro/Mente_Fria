# Roteiro do Vídeo — Mente Fria

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039)
**Link do vídeo (não listado):** https://youtu.be/-NEZsr-lXFI
**Duração final:** 11:22 (entre 8 e 12 min)

## 1. Planejamento dos blocos
| Bloco | Tempo | Quem fala | O que aparece na tela |
|---|---|---|---|
| 1 · Abertura | 0:00–0:41 | Lucas | **Gancho:** tela preta com “Dez quilos de açaí. Vencidos.”. Depois, o cartão de título “Mente Fria” com os 4 nomes |
| 2 · Problema e cliente | 0:41–1:50 | Lucas | As quatro dores do Documento de Visão em cartões, e os dois perfis (Administrador e Funcionário) |
| 3 · Demonstração (deploy público) | 1:50–4:17 | Leonardo → Diogo → Kauê → Leonardo → Diogo → Kauê → Lucas | O sistema publicado, com a URL `kauencarneiro.github.io/Mente_Fria` sempre visível. História de uso em 10 cenas, com destaque no ponto da tela de que cada fala trata |
| 4 · Engenharia: rastreabilidade | 4:17–5:46 | Kauê e Diogo | A US11 seguida de ponta a ponta: Backlog → UML e DER → PR #107 revisado → CT43 e o teste rodando. Mais a decisão D2 |
| 5 · Processo ágil | 5:46–8:05 | Kauê, Diogo, Lucas e Leonardo | O “board”: a equipe não usou quadro (GitHub Projects ou Trello); no lugar dele, a coluna **Sprint alvo** do Backlog, com as mudanças “A → B”. Depois, a tabela planejado × entregue, a ata da Sprint 1 e o que mudou na Sprint 2, o Adendo nº 1, o PR com revisão e a tabela de papéis |
| 6 · Depoimentos individuais | 8:05–10:55 | todos (21 a 84 s cada) | A voz de cada integrante, com o nome e o papel na tela |
| Fechamento | 10:55–11:22 | Diogo | O README do GitHub e depois a tela parada por 5 s: nome, URL do deploy, repositório, tecnologias e os nomes da equipe |

## 2. Requisitos mínimos na demonstração (bloco 3)
| Requisito mínimo | Momento do vídeo | Como aparece |
|---|---|---|
| Autenticação com 2+ perfis | 1:50 e 3:06 | Entrada como Administrador e depois como Funcionário, com o menu menor. O Funcionário digita `#/dashboard` na barra e o sistema nega: “Esta área é exclusiva do Administrador.” |
| Regra de negócio não trivial (caso normal + borda) | 2:16–3:44 | **Normal:** a recomendação de produção por lucro (quantos copos fazer) e o pedido com baixa automática dos ingredientes. **Borda:** o açaí vencido não conta no estoque válido e não pode ser vendido; um pedido maior que o estoque válido é recusado com “Estoque insuficiente…” |
| Consulta agregada (relatório/dashboard) | 4:01 | O Dashboard do mês: faturamento, mais vendidos, ingredientes mais usados e ranking de vendas, com uma frase sobre o que ele soma |
| Validações em interface e banco | 2:10 | Um produto salvo sem açaí recebe “O produto precisa ter o açaí entre os ingredientes.”, e nada é gravado |
| 6+ entidades com N:N | 4:30 (bloco 4) | O DER na tela, com as tabelas `produto_ingrediente` (composição) e `pedido_produto` (itens do pedido) |
| Deploy público (URL visível) | todo o bloco 3 | A URL aparece em todas as cenas: na barra do navegador ou num selo fixo no canto da tela |
| Repositório com README | 10:55 (fechamento) | O README do GitHub rolando até “Sistema publicado”, mais o link na tela final |

## 3. História rastreada (bloco 4)
**História escolhida:** US11 — Visualizar a recomendação de produção

| Etapa | Artefato mostrado | Link |
|---|---|---|
| Requisito | O critério “Explica o zero” no Backlog, que foi reescrito em 15/11: “Falta ingrediente” virou “O estoque não dá ou rende mais em outro copo” | `docs/Backlog_Priorizado_Mente_Fria.docx` (US11) |
| Modelo | O caso de uso UC11 e o DER (`produto`, `produto_ingrediente`, `ingrediente`) | `docs/Diagramas_UML.md` · `docs/DER.md` |
| Código | O PR #107, com a revisão do Leonardo (que rodou a suíte 3 vezes na branch) | https://github.com/KaueNCarneiro/Mente_Fria/pull/107 · `docs/Sprint_4/evidencias/2026-11-15_leonardo_revisao-prs-103-107_npm-test.txt` |
| Teste | O CT43 no Plano de Testes, e o teste de navegador **falhando antes** da correção (exit 1) e **passando depois** (43 verificações) | `docs/relatorio-testes-final.md` (CT43) · `docs/Sprint_4/evidencias/2026-11-15_diogo_texto-quantidade-zero-producao_npm-test.txt` |

**Decisão técnica justificada:** escolhemos o Java chamando o Python como subprocesso, trocando JSON (decisão D2), em vez de um serviço HTTP separado, porque assim o sistema cabe num único serviço no plano gratuito do Render, e o contrato fica atrás de uma interface: se um dia precisar virar serviço, o resto do sistema não muda.

## 4. Evidências de processo ágil (bloco 5)
| Evidência | Sprint | Artefato mostrado na tela |
|---|---|---|
| Planejado × entregue por sprint | 1–4 | Um cartão montado para o vídeo a partir dos relatórios: na Sprint 1, nenhuma história funcional; na 2, uma concluída e as outras em parte; a 3 foi de planejamento, com o código de referência testado; na 4, as 15 obrigatórias, mais a US26 |
| Retrospectiva com ação aplicada | 1 → 2 | A ata da Sprint 1: o repositório desorganizado e o backend sem falar com o banco. As ações foram recriar o repositório por pastas e conectar o Java ao PostgreSQL primeiro. O registro da Sprint 2 mostra as duas aplicadas: estrutura por camada e `GET /api/ingredientes` lendo o banco |
| Mudança de escopo negociada | 3 → 4 | O Adendo nº 1, aprovado pelo professor em 27/10: receita virou lucro; reposição com orçamento; US13, US19 e US27 fora. Mais a US26, mantida pelo PO depois do prazo (A46), entregue em 12/11 e avisada ao professor |
| PR com revisão relevante | 4 | A lista de PRs fechados, com revisão de outro integrante; no bloco 4, o PR #107, que o Leonardo revisou rodando a suíte 3 vezes antes de aprovar |
| Rodízio de papéis | 1–4 | Um cartão com os papéis por sprint. **Os papéis não rodaram**, e isso entra como lição no bloco 6 |

## 5. Depoimentos (bloco 6)
| Integrante | Papéis exercidos | Conceito aprendido na prática | Evidência citada | O que faria diferente |
|---|---|---|---|---|
| Lucas | PO e backend (Sprints 1–4) | MVP e negociação de escopo | O Adendo nº 1 e a decisão A46 (manter a US26 depois do prazo) | Fatiar as histórias menores e integrar o backend desde a Sprint 1 |
| Kauê | Scrum Master e Python (Sprints 1–4) | testes como especificação | O oráculo de força bruta que confere o otimizador em 200 instâncias | Fazer a retrospectiva em reunião em toda sprint |
| Leonardo | Frontend (Sprints 1–4) | revisão de código e critério de aceite | A revisão dos PRs #103 e #107 e o CT43 | Escrever o teste da tela junto com o critério de aceite |
| Diogo | Dados, US26 e deploy (Sprints 1–4) | definição de pronto e o teste que precisa poder falhar | Os testes de constraints “quebrados de propósito” (`2026-10-29_diogo_banco_quebrar-de-proposito.txt`) | Publicar o sistema já na primeira sprint |

## 6. Checagem antes do envio
- [ ] Sem senhas, tokens, `.env` ou dados pessoais reais na tela
- [ ] Autorização de imagem de quem aparece e do cliente (se citado)
- [ ] Link testado em janela anônima
- [ ] Ficha da mostra em `docs/mostra-virtual.md` + capa + 3 capturas enviadas

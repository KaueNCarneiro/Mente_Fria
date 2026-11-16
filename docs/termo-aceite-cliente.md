# Termo de Aceite do Cliente — Mente Fria

**Cliente:** professor da disciplina de Laboratório de Engenharia de Software (ADS, Fatec Ribeirão Preto), que assume formalmente o papel de cliente, como prevê o Manual do Aluno.
**Apresentado por:** Lucas Gabriel Valadares Bassi (RA 2840482423008), Product Owner
**Equipe que acompanhou os testes:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039)
**Data:** ___/___/2026
**Sistema avaliado:** frontend https://kauencarneiro.github.io/Mente_Fria/ · backend https://mente-fria-backend.onrender.com
**Base:** Termo de Aceite do Projeto (E2b, 28/08/2026), com as mudanças do Adendo nº 1 (aprovado em 27/10/2026, `docs/Sprint_3/sprint-3-adendo-termo-de-aceite.md`). Testes em `docs/relatorio-testes-final.md`.

## 1. Escopo avaliado (comparado ao Termo de Aceite do Projeto — E2b)

| Funcionalidade aceita na E2b | Entregue? | Observação |
|---|---|---|
| Cadastro e autenticação de usuários: Administrador e Funcionário com permissões distintas, senha mínima de 8 caracteres, e-mail único e redirecionamento por perfil | Sim | US1–US3. Testada nos dois perfis na URL publicada (CT68, 14/11) |
| Cadastro de ingredientes e produtos: ingredientes com unidade, custo, porção padrão e quantidade mínima; produtos com composição (N:N) | Sim | US4 e US5. A mais: cadastro de unidades e porção padrão por unidade (US26, Tela 19), entregue em 12/11 |
| Cadastro e atualização de preços, restritos ao Administrador | Sim | US6. O Funcionário recebe `403` |
| Controle de estoque e pedidos, com baixa automática dos ingredientes | Sim | US9 e US12. O estoque é controlado por linhas de entrada com validade, e a baixa consome a menor validade primeiro. O pedido é atômico, e ingrediente vencido nunca é vendido. Há descarte de linha vencida |
| Otimização da produção por Programação Linear Inteira pura, **maximizando a receita** | Sim, com mudança aprovada | US10 e US11. **Pelo Adendo nº 1, o objetivo passou a ser o lucro estimado** (preço menos custo dos ingredientes). A tela explica por que um produto ficou com zero, sem termos técnicos. Roda na URL publicada com a licença do Gurobi instalada pelo `pip`, autorizada por escrito pela Gurobi (28/10) |
| Recomendação de reposição: ingredientes a repor e quantidades | Sim | US14. Usa o consumo do período e um orçamento informado (Adendo nº 1, item 2) |
| Alerta de validade: ingredientes próximos do vencimento | Em parte | Opção (a) do Adendo nº 1, item 4. Cada linha de estoque mostra "Vence em breve" (até 7 dias) ou "Vencida", e o dashboard conta os ingredientes com quantidade vencida. Os alertas completos (US15 e US16) ficaram fora do semestre |
| Dashboard gerencial: receita máxima estimada, produtos prioritários, situação do estoque e recomendações | Sim | US17. Mostra o faturamento do mês, os mais vendidos, o ranking de vendas, a situação do estoque e o último plano de produção (receita e lucro estimados) |
| Sugestões de marketing para os produtos prioritários | Sim | US18. Usa a última recomendação de produção |

| Critério de pronto do MVP (E2b) | Atendido? | Observação |
|---|---|---|
| Login como Administrador ou Funcionário, respeitando as permissões | Sim | CT06, CT08 e CT68 |
| Fluxo de ponta a ponta: cadastros → estoque → recomendação e propaganda → pedido → baixa automática | Sim | CT68 na URL publicada (14/11) e aceite da equipe (15/11) |
| Otimização por Programação Linear Inteira pura | Sim | Conferida contra um oráculo de força bruta em 200 instâncias (testes do otimizador) |
| Dashboard com dados reais do sistema | Sim | CT58–CT63 |
| Alertas e recomendações: vencimento próximo e reposição | Em parte | A reposição está completa. O vencimento aparece como situação por linha (ver acima) |
| Sistema disponível publicamente por URL | Sim | Desde 11/11/2026. O banco gratuito do Render fica no ar até 11/12/2026, às 21h59, depois da data-limite de 30/11 |
| README permite configurar e executar o projeto do zero | Sim | CT20 (15/11), feito numa máquina com as ferramentas já instaladas |

## 2. Ressalvas / pendências

- **Fora do semestre (Adendo nº 1, item 6):**
  - US13, clientes;
  - US19, exportar CSV;
  - US27, cancelar pedido;
  - as versões completas da US15 e da US16.
- **US21 a US25:** ficaram como *Won't* no backlog.
- **Mudança de objetivo da otimização:** passou de receita para lucro, aprovada no Adendo nº 1.
- **Entrega fora do calendário:** o sistema foi publicado em 11/11, e não em 02/11, e não houve Sprint Review em 06/11. Os testes no ambiente publicado foram em 14 e 15/11.
- **Vencimento do banco:** o banco gratuito do Render vence em 11/12/2026, às 21h59. Depois disso, o sistema publicado deixa de funcionar, a não ser que a equipe recrie o banco a partir do backup.

## 3. Parecer

[ ] Aceito sem ressalvas
[ ] Aceito com ressalvas (listadas acima, não bloqueantes)
[ ] Não aceito — motivo: ______________________

## 4. Assinatura

- Cliente (professor): _______________________ Data: ___/___/2026
- Product Owner da equipe: Lucas Gabriel Valadares Bassi (RA 2840482423008) _______________________

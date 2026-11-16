# Termo de Aceite do Cliente — Mente Fria

**Cliente/PO:** Professor da disciplina de Laboratório de Engenharia de Software (ADS, Fatec Ribeirão Preto), no papel de cliente, conforme o Manual do Aluno
**Data:** ___/___/2026
**Equipe que acompanhou os testes:** Leonardo Fagundes Oliveira (RA 2840482421009) — Diogo Pereira Miranda (RA 2840482423006) — Lucas Gabriel Valadares Bassi (RA 2840482423008) — Kauê Nogueira Carneiro (RA 2840482423039)

## 1. Escopo avaliado (comparado ao Termo de Aceite do Projeto — E2b)
| Funcionalidade aceita na E2b | Entregue? | Observação do cliente |
|---|---|---|
| Cadastro e autenticação de usuários (Administrador e Funcionário, com permissões distintas) | Sim | |
| Cadastro de ingredientes e produtos (com composição N:N) | Sim | |
| Cadastro e atualização de preços (só o Administrador) | Sim | |
| Controle de estoque e pedidos, com baixa automática dos ingredientes | Sim | |
| Otimização da produção por Programação Linear Inteira pura | Sim, com a mudança do Adendo nº 1 (maximiza o lucro estimado em vez da receita) | |
| Recomendação de reposição | Sim | |
| Alerta de validade | Em parte (situação “Vence em breve” e “Vencida” por linha de estoque; Adendo nº 1, item 4) | |
| Dashboard gerencial | Sim | |
| Sugestões de marketing | Sim | |

## 2. Ressalvas / pendências
- **Otimização:** o objetivo passou de receita para lucro estimado, pelo Adendo nº 1, aprovado em 27/10/2026.
- **Alertas:** os alertas completos de reposição e de validade (US15 e US16) ficaram fora do semestre. O que foi entregue é a opção (a) do Adendo nº 1, item 4.
- **Fora do semestre:** US13 (clientes), US19 (exportar CSV) e US27 (cancelar pedido), pelo Adendo nº 1, item 6. As US21 a US25 ficaram como *Won't* no Backlog.
- **Entregue a mais:** a US26 (porção padrão por unidade de medida), em 12/11/2026.
- **Datas:** o sistema foi publicado em 11/11/2026, e não em 02/11, e não houve Sprint Review em 06/11.
- **Testes:** o resultado está em `docs/relatorio-testes-final.md`.
- **Vencimento do banco:** o banco gratuito do Render vence em 11/12/2026, às 21h59. Depois disso, o sistema publicado deixa de funcionar, a não ser que a equipe recrie o banco a partir do backup.

## 3. Parecer
[ ] Aceito sem ressalvas
[ ] Aceito com ressalvas (listadas acima, não bloqueantes)
[ ] Não aceito — motivo: [...]

## 4. Assinatura
- Cliente/PO: _______________________

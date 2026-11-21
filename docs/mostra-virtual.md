# Mostra Virtual — Mente Fria

**Frase de destaque:** Diz ao dono de uma açaiteria quanto produzir de cada copo para ter o maior lucro com o estoque válido de hoje.
**Resumo:** O dono de uma açaiteria faz tudo à mão: não sabe quais copos dão mais lucro, compra errado e perde ingrediente vencido. O Mente Fria controla o estoque por lote e validade, registra pedidos com baixa automática e recusa vender o que venceu. O diferencial é um modelo de otimização (programação linear inteira, Python + Gurobi) que recomenda quanto produzir de cada copo para o maior lucro e quanto comprar dentro de um orçamento. Tem dois perfis, Administrador e Funcionário, e um dashboard do mês.

## Equipe
| Nome completo | RA | Papéis exercidos (Sprints 1–4) |
|---|---|---|
| Leonardo Fagundes Oliveira | 2840482421009 | Frontend |
| Diogo Pereira Miranda | 2840482423006 | Dados, US26 e publicação (deploy) |
| Lucas Gabriel Valadares Bassi | 2840482423008 | Product Owner e backend |
| Kauê Nogueira Carneiro | 2840482423039 | Scrum Master e otimização em Python |

**Cliente / PO:** dono de uma açaiteria local (nome não divulgado) · PO: Lucas Gabriel Valadares Bassi · **Trilha:** B
**Tecnologias:** Java 25 + Spring Boot 4.0.8 (Spring JDBC), Python 3.14 + Gurobi 13.0.3, JavaScript puro, PostgreSQL 16, Render (backend em Docker e banco) e GitHub Pages (frontend), GitHub Actions (publicação do frontend)

## Links
- Vídeo: https://youtu.be/-NEZsr-lXFI
- Deploy: https://kauencarneiro.github.io/Mente_Fria/
- Repositório: https://github.com/KaueNCarneiro/Mente_Fria

## Aprendizado da equipe
Integrar e publicar cedo. Na Sprint 1 não entregamos nenhuma história funcional, porque cada parte andava sozinha e o backend nem falava com o banco. A retrospectiva nos fez ligar tudo primeiro, mas o sistema só foi publicado no fim. Entregar em toda sprint algo pequeno, integrado e no ar teria evitado o atraso.

## Imagens enviadas
- capa.png (1280×720)
- tela-1.png, tela-2.png, tela-3.png (1920×1080)

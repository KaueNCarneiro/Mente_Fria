# Resposta do professor — mensagem da Sprint 3 (P3)

**Recebida em:** 27/10/2026, em resposta a `sprint-3-mensagem-professor.md` (enviada pelo Diogo).
**Registrada por:** Diogo. O texto abaixo é a resposta como chegou. O que a equipe decidiu a partir dela está em `sprint-3-decisoes-e-pendencias.md` (A30–A33).

---

Olá, Diogo! Tudo bem?
Obrigado pelo aviso e pela organização do planejamento da Sprint 3. Acho positivo que vocês estejam avaliando essas mudanças antes de alterar os documentos já aprovados.

**1. Sobre o adendo ao Termo de Aceite**
Concordo com a proposta de elaborar um adendo datado, preservando o documento original. Descrevam as alterações, suas justificativas e os critérios de aceitação correspondentes, para que fique claro o que foi ajustado e por quê. Depois de alinharmos e aprovarmos o adendo, atualizem também os documentos relacionados, mantendo a rastreabilidade entre as histórias do backlog, os requisitos e os testes.
A mudança da recomendação de produção para priorizar o lucro estimado faz sentido diante do problema de negócio apresentado no Documento de Visão. Apenas deixem explícito como esse lucro será calculado e quais custos serão considerados.
Quanto à reposição e ao dashboard, mantenham os requisitos obrigatórios como prioridade. As funcionalidades adicionais são interessantes, mas não devem comprometer a conclusão e a qualidade das entregas previstas para esta sprint.

**2. Sobre a licença do Gurobi no Render**
Nesse ponto, peço que tenham cautela. A licença restrita que acompanha a instalação do `gurobipy` pelo `pip` é identificada pela própria Gurobi como destinada a uso não produtivo. O fato de os modelos de vocês estarem dentro dos limites de tamanho não significa, por si só, que a licença permita executar o solver em um sistema publicado.
Consultem a documentação oficial e solicitem uma confirmação expressa à Gurobi, explicando que se trata de um projeto acadêmico, sem finalidade comercial, hospedado no Render e acessível para avaliação. Não considerem o uso autorizado até esclarecerem essa questão.
Vocês podem consultar estas referências:
- [Restrições da licença incluída no Gurobi para Python](https://support.gurobi.com/hc/en-us/articles/29682074018833-What-does-Restricted-license-for-non-production-use-only-mean)
- [Licenças e condições de uso da Gurobi](https://www.gurobi.com/product/pricing-and-licensing)

Se não conseguirem a autorização necessária a tempo, planejem uma alternativa para a demonstração, sem depender de uma execução do solver cuja utilização não esteja autorizada.

**3. Sobre as datas de avaliação**
Podem se planejar considerando o dia 30/11 como a data-limite prevista para disponibilidade do sistema. Entretanto, não deixem de preparar um backup do banco de dados e verificar se conseguem restaurá-lo, para evitar que a expiração do serviço gratuito comprometa a avaliação.
Caso seja necessário manter o sistema disponível por mais tempo, alinharemos isso com antecedência.

**4. Sobre uma sprint extra**
Concordo com a ideia de aproveitar o tempo disponível para adiantar algumas histórias de menor prioridade, como o cancelamento de pedidos. No entanto, precisamos tomar cuidado com o prazo e garantir que essa iniciativa não prejudique a qualidade da entrega.
Minha sugestão é que vocês priorizem os testes e as correções das funcionalidades obrigatórias e avaliem, com base no andamento do trabalho, se haverá tempo suficiente para desenvolver funcionalidades adicionais. Se perceberem que o prazo está apertado ou que as funcionalidades principais ainda precisam de ajustes, deixem a sprint extra de lado e concentrem os esforços nos testes, nas correções e na validação do sistema.
O mais importante é entregar um sistema funcional, estável e com os requisitos obrigatórios devidamente atendidos, em vez de tentar incluir novas funcionalidades e comprometer a qualidade do resultado.

No geral, considero o planejamento coerente. O mais importante agora é manter o escopo controlado, documentar adequadamente as mudanças e garantir que a solução esteja funcional e pronta para avaliação.
Obrigado por manterem essa comunicação e bom trabalho a todos!

Abraço,
Professor

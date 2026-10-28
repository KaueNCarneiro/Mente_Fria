# Adendo nº 1 ao Termo de Aceite — Mente Fria (APROVADO)

**Status:** **aprovado e assinado em 27/10/2026** pelo professor, pelo Product Owner e pela equipe (tabela no fim). O texto abaixo da linha é a versão aprovada: **não alterar**. Mudanças futuras viram um novo adendo, datado.
**Uso:** anexar ao Termo de Aceite de 28/08/2026, **sem alterar o Termo original**. Os documentos relacionados (Backlog, Visão, Arquitetura, DER, UML, mapas, Plano de Testes e README) foram atualizados a partir dele em 27/10 (decisão A33).

---

**Termo de Aceite do Projeto — Mente Fria — Adendo nº 1**

**Equipe:** Leonardo Fagundes Oliveira (RA 2840482421009), Diogo Pereira Miranda (RA 2840482423006), Lucas Gabriel Valadares Bassi (RA 2840482423008), Kauê Nogueira Carneiro (RA 2840482423039)
**Termo original:** 28/08/2026 (preservado sem alterações).
**Data deste adendo:** 27/10/2026. **Vigência:** a partir da aprovação do professor.

Este adendo registra os ajustes de escopo da Sprint 3. Para cada ajuste: o texto original, o novo texto, a justificativa, os critérios de aceitação e a rastreabilidade (histórias do Backlog, casos de teste do Plano de Testes e seções do Contrato de Comunicação v2). O restante do Termo continua valendo como está.

## 1. Otimização da produção: maximizar o lucro estimado (US10, US11)

**Texto original (Escopo):** "cálculo da quantidade recomendada de cada produto por Programação Linear Inteira pura, buscando maximizar a **receita** com o estoque disponível".

**Novo texto:** "cálculo da quantidade recomendada de cada produto por Programação Linear Inteira pura, buscando maximizar o **lucro estimado** com o estoque **válido** disponível".

**Justificativa:** a principal dor do cliente, no Documento de Visão, é a lucratividade. Maximizar a receita pode recomendar o produto que mais fatura e menos lucra. O professor concordou com a mudança e pediu que o cálculo fique explícito (abaixo).

**Como o lucro é calculado:**

| Termo | Fórmula |
|---|---|
| Custo de um copo do produto | soma, para cada ingrediente da composição, de `quantidade_utilizada × custo_unitario` |
| **Lucro por copo** | `preço de venda − custo de um copo` |
| **Lucro estimado do plano** | soma, para cada produto, de `quantidade recomendada × lucro por copo` |
| Receita estimada do plano | soma, para cada produto, de `quantidade recomendada × preço de venda` |

- **Custos considerados:** só o custo dos **ingredientes** da composição, pelo `custo_unitario` **atual** do cadastro. As embalagens cadastradas como ingrediente (o copo descartável) entram.
- **Custos não considerados:** mão de obra, aluguel, energia, impostos, taxas de cartão e de aplicativos de entrega, frete, e perdas por vencimento ou descarte. Por isso o valor se chama "lucro **estimado**": é a margem sobre os ingredientes, não o lucro contábil da loja.
- **Estoque usado:** só as linhas de estoque **não vencidas** (estoque válido).
- **Produto com lucro por copo ≤ 0** não é recomendado (fica em 0, e a tela mostra "Sem lucro").
- **Empate:** se mais de um plano dá o mesmo lucro máximo, o sistema mantém esse lucro e decide produto por produto, na ordem de cadastro: primeiro produz o máximo possível do produto cadastrado primeiro, depois o máximo do segundo, e assim por diante.
- Os valores são recalculados no backend com precisão decimal (`BigDecimal`).
- **"Receita máxima estimada"** (citada no escopo do dashboard) passa a significar **a receita do plano de maior lucro**.

**Critérios de aceitação:**
- Numa instância em que o produto de maior receita não é o de maior lucro, o sistema recomenda o de maior lucro (CT41).
- Com o estoque de demonstração, a recomendação é 151 / 1 / 0 copos, lucro estimado de R$ 1.011,71 e receita estimada de R$ 1.966,80 (CT42).
- A tela de produção mostra, por produto, a quantidade, o lucro potencial e a receita estimada, sem termos técnicos de otimização, e explica o zero: "Sem lucro" ou "Falta ingrediente" (CT43).

**Rastreabilidade:** US10, US11 · CT41–CT45 · Contrato v2 §1.1–§1.4 e §2.6.

## 2. Recomendação de reposição com orçamento (US14)

**Texto original (Escopo):** "identificação de ingredientes que precisam ser repostos e sugestão das quantidades necessárias".

**Novo texto:** "identificação de ingredientes que precisam ser repostos e sugestão das quantidades a comprar, **com base no consumo de um período informado e limitada a um orçamento informado**".

**Justificativa:** o Backlog (US14) já pedia o cálculo "com base no consumo de um período X e o preço atual dos ingredientes". O orçamento reflete a compra mensal real do cliente, que tem um valor máximo para gastar.

**Regras:**
- O proprietário informa o período (1 a 365 dias) e o orçamento (R$).
- Consumo = pedidos do período × composição dos produtos.
- A necessidade de cada ingrediente cobre o consumo do período e o estoque mínimo, descontado o estoque válido.
- Se o orçamento cobre tudo, o sistema sugere a necessidade inteira. Se não cobre, reparte o dinheiro de forma equilibrada entre os ingredientes, sem passar do orçamento. Se não cobre nem os mínimos, mostra os mínimos e quanto falta.
- Nada é comprado automaticamente.

**Critérios de aceitação:** CT51–CT57 (orçamento folgado, insuficiente e parcial; período; sem pedidos; validação e permissão; tela sem termos técnicos).

**Rastreabilidade:** US14 · CT51–CT57 · Contrato v2 §1.7 e §2.7.

## 3. Dashboard gerencial (US17)

**Texto original (Escopo):** "visualização da receita máxima estimada, produtos prioritários, situação do estoque e recomendações do sistema".

**Novo texto:** mantém o texto original e detalha o conteúdo:
- **obrigatório (Termo e US17):** receita máxima estimada e produtos prioritários do último cálculo de produção; situação do estoque (ingredientes no mínimo ou abaixo; ingredientes com vencidos); faturamento do mês; produtos mais vendidos no mês; ingredientes mais utilizados no mês, ordenados pelo custo do consumo;
- **adicional:** ranking de vendas por usuário, com filtros por usuário, produto e faixa de valor.

**Justificativa:** o Termo pede a receita máxima estimada e os produtos prioritários; a US17 pede mais vendidos, faturamento mensal e ingredientes mais utilizados. Seguindo a orientação do professor, o ranking e os filtros são **adicionais**: são os primeiros a sair se o prazo apertar. Os ingredientes são ordenados pelo valor gasto porque somar quantidades compara kg com unidades.

**Critérios de aceitação:** só pedidos ativos entram nos números do mês (CT58); com o seed de demonstração, faturamento de R$ 326,40 no mês (CT62); receita máxima estimada e situação do estoque visíveis (CT61); tela do mês (CT63). Adicionais: CT59 e CT60.

**Rastreabilidade:** US17 · CT58–CT63 · Contrato v2 §2.8.

## 4. Alertas de reposição e de validade (US15, US16) — decidido pela equipe em 27/10: opção (a)

**Texto original:** escopo, "Alerta de validade: identificação de ingredientes próximos do vencimento"; critério de pronto, "Alertas e recomendações são gerados corretamente, incluindo ingredientes próximos do vencimento e necessidades de reposição".

**Situação:** as histórias dedicadas aos alertas (US15 e US16, ambas "Should") ficaram fora da Sprint 3, mas o Termo aceitou "Must + Should" e cita os alertas no critério de pronto. Parte do comportamento já está entregue nas telas de estoque e no dashboard:

| Critério do Backlog | Como é atendido hoje |
|---|---|
| US15: comparar a quantidade mínima com a atual e alertar na interface | Selo "Abaixo do mínimo" na tela de estoque, com o estoque **válido**; contagem no dashboard |
| US15: não gerar pedido automático | Atendido (nada é comprado automaticamente) |
| US16: alerta quando faltarem X dias para o vencimento (parametrizável) | Situação "Vence em breve" por linha de estoque, com **X fixo em 7 dias**; validade mais próxima na lista de estoque |
| US16: destacar itens vencidos separadamente | Atendido: situação "Vencida", quantidade vencida separada e contagem no dashboard |
| US16: considerar a data atual do servidor | Atendido (data de São Paulo) |

**Proposta da equipe:** os alertas deste semestre são atendidos pelas telas de estoque e pelo dashboard, como na tabela acima, com o aviso de vencimento **fixo em 7 dias**. As histórias dedicadas (US15 e US16, com o prazo ajustável) ficam para depois. A equipe escolheu assim para manter o escopo controlado, como o professor orientou.

**Critérios de aceitação:** um ingrediente com estoque válido igual ou abaixo do mínimo aparece com o selo "Abaixo do mínimo" na tela de estoque e entra na contagem do dashboard; uma linha que vence em até 7 dias aparece como "Vence em breve"; uma linha vencida aparece como "Vencida", separada, e entra na contagem do dashboard.

**Rastreabilidade:** US15, US16 · CT61 (situação do estoque), CT30–CT36 (estoque por linhas) · Contrato v2 §2.4.

## 5. Publicação do sistema (US20)

**Texto original (critério de pronto):** "Sistema está disponível publicamente por meio de uma URL para acesso ao software". **Stack:** GitHub Pages (frontend) e Render (banco de dados e backend).

**Novo texto:** mantém o texto e acrescenta:
- o sistema fica disponível pela URL publicada **até 30/11/2026**, data-limite indicada pelo professor;
- o banco do plano gratuito do Render expira 30 dias depois de criado. A equipe faz **backup** do banco e já **testou a restauração** (procedimento no tutorial do Kauê, §12.6);
- o solver de otimização só roda no sistema publicado com **licença autorizada pela Gurobi** para esse uso. Sem essa autorização, vale o plano alternativo da equipe (decisão A31): no sistema publicado, as telas de produção, reposição e marketing **e o bloco "Plano de produção" do dashboard** (onde ficam a "receita máxima estimada" e os "produtos prioritários" que este Termo cita) informam que a recomendação está disponível só na demonstração local. **Por isso, enquanto valer esse plano, a recomendação de produção, a reposição e as sugestões de campanha não são demonstradas pela URL publicada, e sim na demonstração local**; a otimização é demonstrada pelo titular da licença acadêmica, no computador dele, como a licença permite. O restante do sistema funciona normalmente na URL publicada. **[atualizar depois da resposta da Gurobi]**

**Critérios de aceitação:** CT67–CT70 (imagem dentro dos limites; fluxo do Termo pela URL publicada; CORS; primeira chamada depois de o serviço "dormir") e um teste de restauração do backup registrado em evidência.

**Rastreabilidade:** US20 · CT67–CT70 · Contrato v2 §2 (Base URL e CORS).

## 6. Histórias fora deste semestre e sprint extra

**Ficam fora da Sprint 3:** US13 (clientes, Could), US19 (exportar CSV, Could), US26 (porção padrão, Could) e a nova **US27 — cancelar pedido** (Could; o cancelamento **não devolve** ingredientes ao estoque). As US15 e US16 seguem o item 4.

**Sprint extra:** com a concordância do professor, a equipe pode adiantar histórias "Could" (por exemplo, a US27) **somente** se as obrigatórias estiverem concluídas, testadas e estáveis. Na dúvida, a prioridade é testar e corrigir.

---

**Aprovação**

| Papel | Nome | Data | Assinatura |
|---|---|---|---|
| Professor | Lucas Baggio Figueira | 27/10/2026 | Lucas Professor |
| Product Owner | Lucas Gabriel Valadares Bassi | 27/10/2026 | Lucas PO |
| Equipe | Diogo, Kauê, Leonardo | 27/10/2026 | Equipe Mente Fria |

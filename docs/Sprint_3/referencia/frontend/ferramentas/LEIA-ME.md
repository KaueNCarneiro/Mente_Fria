# Ferramentas de verificação do frontend (sem navegador)

Complemento ao Playwright: roda o `app.js` em um DOM simulado (jsdom) com `fetch` simulado e compara o HTML das telas.
**Não substitui** os testes do Playwright nem o aceite com o backend real.

**Instalação** (uma vez). O Node procura o `jsdom` a partir da pasta do script, por isso a pasta `ferramentas` vai **para dentro de `frontend/`**:
```powershell
cd frontend
mkdir ferramentas
copy ..\docs\Sprint_3\referencia\frontend\ferramentas\*.mjs ferramentas\
npm install jsdom --no-save          # não entra no package.json
```
**Uso** (os retratos `.json` são arquivos temporários: **não comite**):
```powershell
node ferramentas\retratar_telas.mjs . retrato_antes.json       # ANTES de refatorar
# ... refatore ...
node ferramentas\retratar_telas.mjs . retrato_depois.json
node ferramentas\comparar_retratos.mjs retrato_antes.json retrato_depois.json
```
Resultado esperado quando nada mudou: `IGUAIS: o HTML, o título e a rota de todas as telas são idênticos.`
`node ferramentas\testar_guarda.mjs .` mostra quais rotas o Funcionário e o Administrador conseguem abrir.

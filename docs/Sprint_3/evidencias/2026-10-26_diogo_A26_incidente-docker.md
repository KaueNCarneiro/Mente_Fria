\# A26 — Docker Desktop não inicia (sailor-ingest.sock)



\- Ocorrências: 26/10/2026 e 30/10/2026, nesta máquina (Windows 11, build 26300).

\- Sintoma: janela de erro "starting services: initializing Ingest server: listening on

&#x20; unix://C:/Users/diogo/AppData/Local/Docker/run/sailor-ingest.sock: rename … The file cannot be

&#x20; accessed by the system", com os botões Quit e Reset to factory defaults.

\- Causa provável: arquivos de socket inacessíveis nas pastas %LOCALAPPDATA%\\Docker\\run e

&#x20; %LOCALAPPDATA%\\docker-secrets-engine. Hipótese, não comprovada: a versão Insider do Windows.

\- Contorno (verificado nas duas vezes): Quit (nunca Reset to factory defaults, que apaga o

&#x20; mente-fria-db); wsl --shutdown; renomear as duas pastas; abrir o Docker de novo.

\- Não é o BD01 da Sprint 2.


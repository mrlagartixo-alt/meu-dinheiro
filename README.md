# Meu Dinheiro — Android v2

Versão 2 do aplicativo de organização financeira.

## Recursos da v2
- Dashboard com saldo, entradas e despesas.
- Separação de despesas essenciais e não essenciais.
- Registro por linguagem natural.
- Categorias automáticas.
- Histórico.
- Filtro por 30 dias.
- Orçamento mensal.
- Limite de gasto não essencial.
- Contas futuras.
- Gráficos de barras e pizza.
- Indicador "Posso gastar?" baseado no saldo e no orçamento.
- Edição/exclusão de lançamentos.
- Persistência local com Room.

## Recursos adicionais implementados
Gráfico de pizza por categoria, barras de entradas x gastos por mês, contas futuras
(com "pagar" que gera despesa), edição e exclusão com confirmação, orçamento salvo no
aparelho e "Posso gastar?" considerando saldo, contas dos próximos 30 dias, orçamento e limite não essencial.

## Gerar o APK pelo GitHub
1. Crie um repositório e envie TODO o conteúdo desta pasta (inclusive `.github`).
2. Aba **Actions** > **Build APK** (roda sozinho a cada push; ou "Run workflow").
3. Ao terminar (~5 min), baixe o APK em **Releases** > "Meu Dinheiro (APK mais recente)" > `MeuDinheiro.apk`.

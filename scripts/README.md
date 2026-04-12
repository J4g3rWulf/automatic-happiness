# Scripts de administração do RecycleApp

## populate_firestore.py

Script Python para popular e gerenciar a coleção `recycling_points` no Cloud Firestore.

### Pré-requisitos

- Python 3.x instalado
- Dependência: `pip install firebase-admin`
- Arquivo `serviceAccountKey.json` na mesma pasta do script
  (gerado no Firebase Console → Configurações → Contas de serviço → Gerar nova chave privada)

> ⚠️ O `serviceAccountKey.json` **nunca deve ser commitado** — ele já está no `.gitignore`.

### Como usar

**Adicionar pontos:**
1. Adicione o novo ponto na lista `points` do script
2. Execute: `python populate_firestore.py`

**Remover pontos:**
1. Adicione o ID do ponto em `to_delete` ... (Exemplo:to_delete = ["pev_bangu"])
2. Remova o ponto da lista `points`
3. Execute: `python populate_firestore.py`
4. Esvazie `to_delete = []`

### Estrutura de um ponto

```python
{
    "id":        "pev_exemplo",           # identificador único snake_case
    "name":      "PEV Exemplo",           # nome exibido no app
    "address":   "Rua Exemplo, 123",      # endereço completo
    "latitude":  -22.9068,               # coordenada
    "longitude": -43.1729,               # coordenada
    "type":      "PEV",                  # PEV | ECOPONTO | ECOPONTO_LIGHT
    "materials": ["Papel", "Plástico", "Vidro", "Metal"]
}
```

### Tipos disponíveis

Os tipos válidos para o campo `type` são os definidos em `PointType.kt`:
`PEV`, `ECOPONTO`, `ECOPONTO_LIGHT`

Os materiais aceitos por cada ponto são livres — use strings descritivas
que serão exibidas diretamente no app.
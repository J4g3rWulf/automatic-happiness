package br.recycleapp.data.map

import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint

/**
 * Lista estática de pontos de coleta verificados manualmente.
 * Usada como fallback quando a Places API não retorna resultados.
 *
 * Organização por tipo:
 * - PEVs da Comlurb
 * - Ecopontos da Comlurb
 * - Ecopontos Light Recicla
 */
internal object RecyclingPointsData {

    // ── PEVs da Comlurb ──────────────────────────────────────────────────────
    private val pevs = listOf(
        RecyclingPoint(
            id        = "pev_comlurb_bangu",
            name      = "PEV Bangu",
            address   = "Rua Roque Barbosa, 348 - Bangu",
            latitude  = -22.85046155285499,
            longitude = -43.46413468350324,
            materials = listOf("Papel", "Plástico", "Vidro", "Metal"),
            type      = PointType.PEV_COMLURB
        ),
        RecyclingPoint(
            id        = "pev_comlurb_madureira",
            name      = "PEV Madureira",
            address   = "Sob o Viaduto Prefeito Negrão de Lima - Madureira",
            latitude  = -22.87516362916978,
            longitude = -43.33496706988172,
            materials = listOf("Papel", "Plástico", "Vidro", "Metal"),
            type      = PointType.PEV_COMLURB
        ),
        RecyclingPoint(
            id        = "pev_comlurb_tijuca",
            name      = "PEV Tijuca",
            address   = "Rua Dr. Renato Rocco, 400 - Tijuca",
            latitude  = -22.927124968864515,
            longitude = -43.229079230075605,
            materials = listOf("Papel", "Plástico", "Vidro", "Metal"),
            type      = PointType.PEV_COMLURB
        ),
    )

    // ── Ecopontos Comlurb ─────────────────────────────────────────────────────
    private val ecopontosComlurb = listOf(
        RecyclingPoint(
            id        = "ecoponto_comlurb_arara",
            name      = "Ecoponto Arará",
            address   = "Rua Aloysio Amancio, s/n, Benfica (Favela do Arará)",
            latitude  = -22.88947222,
            longitude = -43.24230556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_barreira_do_vasco",
            name      = "Ecoponto Barreira do Vasco",
            address   = "Rua Bela, s/n, Vasco da Gama (São Cristóvão)",
            latitude  = -22.88697222,
            longitude = -43.22652778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_fogueteiro",
            name      = "Ecoponto Fogueteiro",
            address   = "Rua Barão de Petrópolis, 786, Santa Teresa",
            latitude  = -22.93238889,
            longitude = -43.20138889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_jupara",
            name      = "Ecoponto Jupará",
            address   = "Rua Jupará, Mangueira",
            latitude  = -22.90194444,
            longitude = -43.23625,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_mangueira",
            name      = "Ecoponto Mangueira",
            address   = "Rua Visconde de Niterói, 800, Mangueira",
            latitude  = -22.90536111,
            longitude = -43.23988889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_mineira",
            name      = "Ecoponto Mineira",
            address   = "Rua Van Erven, 135, Catumbi",
            latitude  = -22.91794444,
            longitude = -43.19730556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_providencia",
            name      = "Ecoponto Providência",
            address   = "Rua Barão da Gamboa, 24, Gamboa (Santo Cristo)",
            latitude  = -22.89836111,
            longitude = -43.19780556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_sao_carlos",
            name      = "Ecoponto São Carlos",
            address   = "Rua São Diniz, S/N, Estácio",
            latitude  = -22.91566667,
            longitude = -43.20197222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vila_dos_sonhos",
            name      = "Ecoponto Vila dos Sonhos",
            address   = "No final da Rua Carmelita da Conceição, Caju (São Sebastião)",
            latitude  = -22.87741667,
            longitude = -43.22130556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_acari",
            name      = "Ecoponto Acari",
            address   = "Rua Roberto Carlos, s/n, Acari",
            latitude  = -22.82144444,
            longitude = -43.34188889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_agua_de_ouro",
            name      = "Ecoponto Água de Ouro",
            address   = "Rua General Cândido, 56, Inhaúma",
            latitude  = -22.88063889,
            longitude = -43.28088889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_alemao",
            name      = "Ecoponto Alemão",
            address   = "Rua Nova, em frente a estação do Itararé, Complexo do Alemão",
            latitude  = -22.86152778,
            longitude = -43.27180556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_av_dom_helder_camara_suipa",
            name      = "Ecoponto Av. Dom Helder Câmara - Suípa",
            address   = "Avenida Dom Helder Câmara, Jacarezinho (Jacaré)",
            latitude  = -22.88408333,
            longitude = -43.25366667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_boogie_woogie",
            name      = "Ecoponto Boogie Woogie",
            address   = "Rua dos Monjolos, com Campo de São João, Pitangueiras",
            latitude  = -22.81727778,
            longitude = -43.1815,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_camarista_meier",
            name      = "Ecoponto Camarista Méier",
            address   = "Rua Camarista Méier, 850, Engenho de Dentro",
            latitude  = -22.91194444,
            longitude = -43.29744444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_coelho_neto",
            name      = "Ecoponto Coelho Neto",
            address   = "Rua Aratangi, s/n, Colégio",
            latitude  = -22.83494444,
            longitude = -43.34063889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_comunidade_do_guarda",
            name      = "Ecoponto Comunidade do Guarda",
            address   = "Rua Ministro Mavignier, Del Castilho",
            latitude  = -22.88136111,
            longitude = -43.28130556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_costa_barros",
            name      = "Ecoponto Costa Barros",
            address   = "Estrada de Botafogo, próximo à UPA, Costa Barros",
            latitude  = -22.82138889,
            longitude = -43.36686111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_fazendinha_campo_do_seu_ze",
            name      = "Ecoponto Fazendinha Campo do Seu Zé",
            address   = "Rua Austregésilo, s/n, Complexo do Alemão",
            latitude  = -22.86361111,
            longitude = -43.27911111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_fazendinha_praca_da_paloma",
            name      = "Ecoponto Fazendinha Praça da Paloma",
            address   = "Rua Austregésilo, 311, Complexo do Alemão",
            latitude  = -22.86563889,
            longitude = -43.2775,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_guarabu",
            name      = "Ecoponto Guarabu",
            address   = "Rua Berna, lado oposto ao n°142, Jardim Carioca",
            latitude  = -22.80347222,
            longitude = -43.19452778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_juramento",
            name      = "Ecoponto Juramento",
            address   = "Praça Cotigi, 200, Vicente de Carvalho",
            latitude  = -22.85672222,
            longitude = -43.31630556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_kelson",
            name      = "Ecoponto Kelson",
            address   = "Rua Kelson, s/n, Penha Circular",
            latitude  = -22.82297222,
            longitude = -43.27475,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_madureira",
            name      = "Ecoponto Madureira",
            address   = "Rua João Pereira, 63, Madureira",
            latitude  = -22.87494444,
            longitude = -43.33461111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_merindiba",
            name      = "Ecoponto Merindiba",
            address   = "Rua Jacupema, s/n próximo ao largo da Penha, Penha",
            latitude  = -22.84608333,
            longitude = -43.27777778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_paim_pamplona",
            name      = "Ecoponto Paim Pamplona",
            address   = "Rua Paim Pamplona, Sampaio",
            latitude  = -22.89777778,
            longitude = -43.26083333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pantoja",
            name      = "Ecoponto Pantoja",
            address   = "Rua Pastor Martin Luther King Jr., s/n, Acari (Coelho Neto)",
            latitude  = -22.82897222,
            longitude = -43.34552778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_para_pedro",
            name      = "Ecoponto Para-Pedro",
            address   = "Estrada da Pedreira, esquina com Travessa do Colégio, Colégio",
            latitude  = -22.83772222,
            longitude = -43.33525,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_parada_de_lucas",
            name      = "Ecoponto Parada de Lucas",
            address   = "Avenida Brasil, 14.000, Parada de Lucas",
            latitude  = -22.814,
            longitude = -43.29366667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_paranapua",
            name      = "Ecoponto Paranapuã",
            address   = "Avenida Paranapuã, s/n, Tauá (Cova da Onça)",
            latitude  = -22.79594444,
            longitude = -43.17944444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_parque_proletario",
            name      = "Ecoponto Parque Proletário",
            address   = "Praça São Lucas, 1 A, Penha",
            latitude  = -22.84669444,
            longitude = -43.28441667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_parque_royal",
            name      = "Ecoponto Parque Royal",
            address   = "Estrada Governador Chagas Freitas, s/n, Portuguesa",
            latitude  = -22.79611111,
            longitude = -43.20905556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pedreira",
            name      = "Ecoponto Pedreira",
            address   = "Rua Pastor Martin Luther King Jr., 11537, Parque Colúmbia (Pavuna)",
            latitude  = -22.82188889,
            longitude = -43.35136111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_piscinao_de_ramos",
            name      = "Ecoponto Piscinão de Ramos",
            address   = "Avenida Guanabara, s/n, Maré",
            latitude  = -22.83944444,
            longitude = -43.25283333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pixunas",
            name      = "Ecoponto Pixunas",
            address   = "Avenida Doutor Agenor de Almeida Loyola, Freguesia (Ilha)",
            latitude  = -22.78744444,
            longitude = -43.17486111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_praca_do_amarelinho",
            name      = "Ecoponto Praça do Amarelinho",
            address   = "Avenida Brasil, 18500, Acari (Irajá / Amarelinho)",
            latitude  = -22.82647222,
            longitude = -43.33966667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_praia_da_rosa",
            name      = "Ecoponto Praia da Rosa",
            address   = "Praia da Rosa, Tauá",
            latitude  = -22.79361111,
            longitude = -43.18777778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_predinhos",
            name      = "Ecoponto Predinhos",
            address   = "Conjunto da Embratel, Manguinhos",
            latitude  = -22.88430556,
            longitude = -43.24477778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_quatro_bicas",
            name      = "Ecoponto Quatro Bicas",
            address   = "Rua Paul Muller, com Rua Professor Otávio Freitas, Penha",
            latitude  = -22.84619444,
            longitude = -43.27961111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_querosene",
            name      = "Ecoponto Querosene",
            address   = "Rua Maestro Arturo Tosacanini, Tauá",
            latitude  = -22.79841667,
            longitude = -43.18241667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_sargento_silvio_hollenbach",
            name      = "Ecoponto Sargento Silvio Hollenbach",
            address   = "Rua Sargento Sílvio Hollenbach, Barros Filho (Fazenda Botafogo)",
            latitude  = -22.82633333,
            longitude = -43.36019444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_thomas_coelho",
            name      = "Ecoponto Thomás Coelho",
            address   = "Avenida Pastor Martin Luther King Jr., Vicente de Carvalho",
            latitude  = -22.86077778,
            longitude = -43.30730556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_tribo",
            name      = "Ecoponto Tribo",
            address   = "Rua Cabo Fleury, s/n, Cocotá (Tribo / Dendê)",
            latitude  = -22.80325,
            longitude = -43.18275,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vigario_geral",
            name      = "Ecoponto Vigário Geral",
            address   = "Rua Doutor Adauto (entrada da comunidade), Vigário Geral",
            latitude  = -22.80330556,
            longitude = -43.30427778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vila_joaniza",
            name      = "Ecoponto Vila Joaniza",
            address   = "Estrada das Canárias, s/n, Galeão",
            latitude  = -22.81136111,
            longitude = -43.22791667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_campo_grande_viaduto",
            name      = "Ecoponto Campo Grande",
            address   = "Avenida Maria Teresa, embaixo do viaduto de Campo Grande",
            latitude  = -22.90425,
            longitude = -43.56605556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_campo_grande_rua",
            name      = "Ecoponto Campo Grande II",
            address   = "Rua Laudelino Campos com Rua Campo Grande",
            latitude  = -22.90302778,
            longitude = -43.56655556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_catiri",
            name      = "Ecoponto Catiri",
            address   = "Rua Roque Barbosa, 390, Bangu",
            latitude  = -22.85066667,
            longitude = -43.46372222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_cinco_marias",
            name      = "Ecoponto Cinco Marias",
            address   = "Estrada do Magarça, 8.487, Guaratiba",
            latitude  = -22.97838889,
            longitude = -43.63897222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_estrada_de_urucania",
            name      = "Ecoponto Estrada de Urucânia",
            address   = "Rua Adalberto Mortati, s/n, Santa Cruz",
            latitude  = -22.91752778,
            longitude = -43.65425,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_jacques_ouriques",
            name      = "Ecoponto Jacques Ouriques",
            address   = "Rua Jacques Ouriques, Padre Miguel",
            latitude  = -22.86852778,
            longitude = -43.444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_jardim_novo",
            name      = "Ecoponto Jardim Novo",
            address   = "Rua Salvador Sabaté, 237, Realengo",
            latitude  = -22.888,
            longitude = -43.418,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_morar_carioca_do_aco",
            name      = "Ecoponto Morar Carioca do Aço",
            address   = "Rua Nassapê, 120, Santa Cruz",
            latitude  = -22.93330556,
            longitude = -43.64980556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pedra_de_guaratiba",
            name      = "Ecoponto Pedra de Guaratiba",
            address   = "Rua Belchior da Fonseca, 267, Pedra de Guaratiba",
            latitude  = -22.99997222,
            longitude = -43.64194444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_petrarca",
            name      = "Ecoponto Petrarca",
            address   = "Praça Petrarca - Avenida Brasil, 32.327, Bangu",
            latitude  = -22.85941667,
            longitude = -43.46136111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_realengo",
            name      = "Ecoponto Realengo",
            address   = "Rua Bernardo Vasconcelos, 1746, Realengo",
            latitude  = -22.87522222,
            longitude = -43.43841667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_sao_tome",
            name      = "Ecoponto São Tomé",
            address   = "Rua São Tomé, 171, Santa Cruz",
            latitude  = -22.92458333,
            longitude = -43.69466667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vagao",
            name      = "Ecoponto Vagão",
            address   = "Avenida Brasil com Rua Recife, Realengo",
            latitude  = -22.86408333,
            longitude = -43.43655556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vila_alianca",
            name      = "Ecoponto Vila Aliança",
            address   = "Rua Coronel Tamarindo, 1960, Bangu",
            latitude  = -22.87536111,
            longitude = -43.47377778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vila_jurema",
            name      = "Ecoponto Vila Jurema",
            address   = "Rua Itajaí, 249, Realengo",
            latitude  = -22.86313889,
            longitude = -43.43813889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vila_kennedy",
            name      = "Ecoponto Vila Kennedy",
            address   = "Avenida Brasil, 38.048, Bangu",
            latitude  = -22.85633333,
            longitude = -43.49577778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vila_vintem",
            name      = "Ecoponto Vila Vintém",
            address   = "Rua Santo Everardo, ao lado do CIEP, Padre Miguel",
            latitude  = -22.87411111,
            longitude = -43.4475,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_areal",
            name      = "Ecoponto Areal",
            address   = "Avenida Governador Leonel de Moura Brizola, Jacarepaguá",
            latitude  = -22.97691667,
            longitude = -43.33794444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_ayrton_senna",
            name      = "Ecoponto Ayrton Senna",
            address   = "Avenida Ayrton Senna, ao lado do Assaí, Gardênia Azul",
            latitude  = -22.95866667,
            longitude = -43.35691667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_beira_rio_pantanal",
            name      = "Ecoponto Beira Rio - Pantanal",
            address   = "Rua Célia Ribeiro da Silva Mendes, Recreio do Bandeirantes",
            latitude  = -23.00127778,
            longitude = -43.44208333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_beira_rio_servidao_g7",
            name      = "Ecoponto Beira Rio - Servidão G7",
            address   = "Rua da Servidão - G7, Recreio dos Bandeirantes",
            latitude  = -23.00330556,
            longitude = -43.45097222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_cdd_karate",
            name      = "Ecoponto CDD Karatê",
            address   = "Avenida Arroio Fundo, Jacarepaguá",
            latitude  = -22.95461111,
            longitude = -43.36561111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_chacrinha",
            name      = "Ecoponto Chacrinha",
            address   = "Estrada Comandante Luis Souto, Praça Seca",
            latitude  = -22.9045,
            longitude = -43.3575,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_cidade_de_deus",
            name      = "Ecoponto Cidade de Deus",
            address   = "Avenida José de Arimatéia, Cidade de Deus",
            latitude  = -22.95119444,
            longitude = -43.36236111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_conjunto_habitacional_bandeirantes",
            name      = "Ecoponto Conjunto Habitacional Bandeirantes",
            address   = "Estrada dos Bandeirantes, 11227, Vargem Pequena",
            latitude  = -22.98886111,
            longitude = -43.43344444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_curva_do_pinheiro",
            name      = "Ecoponto Curva do Pinheiro",
            address   = "Estrada de Jacarepaguá, 4460, Jacarepaguá (Rio das Pedras)",
            latitude  = -22.96830556,
            longitude = -43.33583333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_gilka_machado",
            name      = "Ecoponto Gilka Machado",
            address   = "Rua Gilka Machado, Recreio dos Bandeirantes (Terreirão)",
            latitude  = -23.02861111,
            longitude = -43.47341667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_mont_serrat",
            name      = "Ecoponto Mont Serrat",
            address   = "Final da Rua Cláudio Jacoby, Vargem Pequena",
            latitude  = -22.97927778,
            longitude = -43.46422222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_muzema",
            name      = "Ecoponto Muzema",
            address   = "Avenida Engenheiro Souza Filho com Estrada de Jacarepaguá, Itanhangá",
            latitude  = -22.98861111,
            longitude = -43.32347222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_nova_esperanca",
            name      = "Ecoponto Nova Esperança",
            address   = "Rua Bocaiúva Cunha, s/n, Gardênia Azul",
            latitude  = -22.95744444,
            longitude = -43.35397222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_novo_lar",
            name      = "Ecoponto Novo Lar",
            address   = "Avenida das Américas, 1900, Vargem Grande",
            latitude  = -23.01969444,
            longitude = -43.49877778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pinheiro",
            name      = "Ecoponto Pinheiro",
            address   = "Estrada de Jacarepaguá, 3.502, Jacarepaguá (Rio das Pedras)",
            latitude  = -22.97194444,
            longitude = -43.33244444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_rio_das_pedras",
            name      = "Ecoponto Rio das Pedras",
            address   = "Avenida Engenheiro Souza Filho, Itanhangá",
            latitude  = -22.97808333,
            longitude = -43.33422222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_santa_maria",
            name      = "Ecoponto Santa Maria",
            address   = "Ladeira Santa Maria, Jacarepaguá (Rio das Pedras)",
            latitude  = -22.91530556,
            longitude = -43.42180556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_sertao",
            name      = "Ecoponto Sertão",
            address   = "Estrada do Sertão, 859, Jacarepaguá",
            latitude  = -22.96652778,
            longitude = -43.32372222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_teixeira_brandao",
            name      = "Ecoponto Teixeira Brandão",
            address   = "Avenida Teixeira Brandão, Jacarepaguá",
            latitude  = -22.94813889,
            longitude = -43.39405556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_terreirao",
            name      = "Ecoponto Terreirão",
            address   = "Avenida Guiomar Novaes, Recreio dos Bandeirantes",
            latitude  = -23.02461111,
            longitude = -43.48188889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_via_light",
            name      = "Ecoponto Via Light",
            address   = "Via Light Rio das Pedras, Itanhangá",
            latitude  = -22.97644444,
            longitude = -43.33225,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_199",
            name      = "Ecoponto 199",
            address   = "Estrada da Gávea, 199, Rocinha",
            latitude  = -22.98575,
            longitude = -43.2435,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_biquinha_vidigal",
            name      = "Ecoponto Biquinha Vidigal",
            address   = "Avenida João Goulart, s/n, Vidigal",
            latitude  = -22.99597222,
            longitude = -43.24055556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_borda_do_mato",
            name      = "Ecoponto Borda do Mato",
            address   = "Rua Borda do Mato, Grajaú",
            latitude  = -22.92855556,
            longitude = -43.26508333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_chacara_do_ceu_vidigal",
            name      = "Ecoponto Chácara do Céu",
            address   = "Rua Aperana, Parque dois Irmãos, Vidigal",
            latitude  = -22.98994444,
            longitude = -43.23322222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_chacara_do_ceu_borel",
            name      = "Ecoponto Chácara do Céu - Borel",
            address   = "Estrada da Caixa D'Água, 47, Tijuca",
            latitude  = -22.93541667,
            longitude = -43.2535,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_cruzeiro_do_sul",
            name      = "Ecoponto Cruzeiro do Sul",
            address   = "Rua Cruzeiro do Sul, 296, Catete (Comunidade Tavares Bastos)",
            latitude  = -22.92677778,
            longitude = -43.18208333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_mata_machado",
            name      = "Ecoponto Mata Machado",
            address   = "Estrada de Furnas, Alto da Boa Vista (Comunidade de Furnas)",
            latitude  = -22.97475,
            longitude = -43.28591667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_morro_do_cruz",
            name      = "Ecoponto Morro do Cruz",
            address   = "Rua Tenente Marques de Sousa, Andaraí",
            latitude  = -22.93075,
            longitude = -43.25055556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_nova_divineia",
            name      = "Ecoponto Nova Divinéia",
            address   = "Rua Alfredo Pujol, na Comunidade Nova Divinéia, Grajaú",
            latitude  = -22.92841667,
            longitude = -43.26355556,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pavao_pavaozinho",
            name      = "Ecoponto Pavão Pavãozinho",
            address   = "Estrada do Cantagalo, 80, Copacabana",
            latitude  = -22.981,
            longitude = -43.19663889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pedro_americo_santo_amaro",
            name      = "Ecoponto Pedro Américo (Santo Amaro)",
            address   = "Rua Pedro Américo, 77, Catete (Comunidade Santo Amaro)",
            latitude  = -22.92394444,
            longitude = -43.17816667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pedro_americo_715",
            name      = "Ecoponto Pedro Américo 715",
            address   = "Rua Pedro Américo, 715, Catete",
            latitude  = -22.92502778,
            longitude = -43.18286111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_pereira_da_silva",
            name      = "Ecoponto Pereira da Silva",
            address   = "Final da Rua Pereira da Silva, Laranjeiras",
            latitude  = -22.93113889,
            longitude = -43.19158333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_recanto_do_trovador",
            name      = "Ecoponto Recanto do Trovador",
            address   = "Rua Amando Albuquerque, 323, Vila Isabel (Morro dos Macacos)",
            latitude  = -22.91588889,
            longitude = -43.25941667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_rocinha_pastor_almir",
            name      = "Ecoponto Rocinha - Pastor Almir",
            address   = "Estrada da Gávea, 486, Rocinha",
            latitude  = -22.98897222,
            longitude = -43.25077778,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_roupa_suja",
            name      = "Ecoponto Roupa Suja",
            address   = "Auto Estrada Lagoa Barra, saída do túnel Zuzu Angel, São Conrado",
            latitude  = -22.99233333,
            longitude = -43.24963889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_rua_1",
            name      = "Ecoponto Rua 1",
            address   = "Estrada da Gávea com Rua 1, Rocinha",
            latitude  = -22.98669444,
            longitude = -43.24558333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_sa_viana",
            name      = "Ecoponto Sá Viana",
            address   = "Rua Sá Viana, Grajaú",
            latitude  = -22.92913889,
            longitude = -43.26158333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_salgueiro_i",
            name      = "Ecoponto Salgueiro I",
            address   = "Rua General Rocca, 99, Tijuca (Salgueiro)",
            latitude  = -22.92827778,
            longitude = -43.22822222,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_salgueiro_ii",
            name      = "Ecoponto Salgueiro II",
            address   = "Rua Francisco Garça, s/n, Tijuca (Salgueiro)",
            latitude  = -22.93066667,
            longitude = -43.22669444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_santo_amaro",
            name      = "Ecoponto Santo Amaro",
            address   = "Rua Santo Amaro, em frente n°349, Catete",
            latitude  = -22.92397222,
            longitude = -43.18141667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_sistema_lagunar",
            name      = "Ecoponto Sistema Lagunar",
            address   = "Avenida Borges de Medeiros, Lagoa (Próximo ao clube Piraquê)",
            latitude  = -22.96930556,
            longitude = -43.217,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_tavares_bastos",
            name      = "Ecoponto Tavares Bastos",
            address   = "Rua Tavares Bastos, Catete",
            latitude  = -22.92791667,
            longitude = -43.18319444,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_turano",
            name      = "Ecoponto Turano",
            address   = "Rua Joaquim Pizarro, 2, Tijuca",
            latitude  = -22.92244444,
            longitude = -43.21533333,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_umuarama",
            name      = "Ecoponto Umuarama",
            address   = "Rua Umuarama, Rocinha",
            latitude  = -22.98586111,
            longitude = -43.24238889,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_valao",
            name      = "Ecoponto Valão",
            address   = "Rua do Valão, Rocinha",
            latitude  = -22.99033333,
            longitude = -43.24886111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vila_verde",
            name      = "Ecoponto Vila Verde",
            address   = "Estrada da Gávea, 525, Rocinha",
            latitude  = -22.98961111,
            longitude = -43.25241667,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
        RecyclingPoint(
            id        = "ecoponto_comlurb_vitoria_regia",
            name      = "Ecoponto Vitória Régia",
            address   = "Rua Vitória Régia, Lagoa",
            latitude  = -22.96483333,
            longitude = -43.19736111,
            materials = listOf("Lixo domiciliar", "Entulho", "Bens inservíveis", "Galhadas"),
            type      = PointType.ECOPONTO_COMLURB
        ),
    )

    // ── Ecopontos Light Recicla ───────────────────────────────────────────────
    private val ecopontosLight = listOf(
        RecyclingPoint(
            id        = "ecoponto_light_espaco_ciencia_viva",
            name      = "Ecoponto Light Recicla - Espaço Ciência Viva",
            address   = "Av. Heitor Beltrão, 321 - Tijuca, Rio de Janeiro",
            latitude  = -22.92252778,
            longitude = -43.22980556,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
        RecyclingPoint(
            id        = "ecoponto_light_shopping_carioca",
            name      = "Ecoponto Light Recicla - Shopping Carioca",
            address   = "Av. Vicente de Carvalho, 909 - Vila da Penha, Rio de Janeiro",
            latitude  = -22.84997222,
            longitude = -43.31102778,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
        RecyclingPoint(
            id        = "ecoponto_light_humaita",
            name      = "Ecoponto Light Recicla - Humaitá",
            address   = "R. Humaitá, 19 - Humaitá, Rio de Janeiro",
            latitude  = -22.95397222,
            longitude = -43.19700000,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
        RecyclingPoint(
            id        = "ecoponto_light_plano_inclinado",
            name      = "Ecoponto Light Recicla - Plano Inclinado",
            address   = "R. Mal. Francisco de Moura, s/nº - Santa Marta, Rio de Janeiro",
            latitude  = -22.94877778,
            longitude = -43.19294444,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
        RecyclingPoint(
            id        = "ecoponto_light_sao_carlos",
            name      = "Ecoponto Light Recicla - São Carlos",
            address   = "Rua Estácio de Sá, esq. com Rua Hélio Beltrão, próx. ao nº 60 - Estácio",
            latitude  = -22.91375000,
            longitude = -43.20458333,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
        RecyclingPoint(
            id        = "ecoponto_light_assai_duque_de_caxias",
            name      = "Ecoponto Light Recicla - Assaí Duque de Caxias",
            address   = "Av. Gov. Leonel Moura Brizola, 2973 - Vila Centenário, Duque de Caxias",
            latitude  = -22.77627778,
            longitude = -43.30863889,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
        RecyclingPoint(
            id        = "ecoponto_light_assai_ilha_do_governador",
            name      = "Ecoponto Light Recicla - Assaí Ilha do Governador",
            address   = "Av. Maestro Paulo e Silva, 100 - Jardim Carioca, Rio de Janeiro",
            latitude  = -22.80305556,
            longitude = -43.20294444,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
        RecyclingPoint(
            id        = "ecoponto_light_lar_frei_luiz",
            name      = "Ecoponto Light Recicla - Lar Frei Luiz",
            address   = "Estr. da Boiúna, 1367 - Taquara, Rio de Janeiro",
            latitude  = -22.91163889,
            longitude = -43.40591667,
            materials = listOf("Plástico", "Metal", "Vidro", "Papel", "Óleo vegetal"),
            type      = PointType.ECOPONTO_LIGHT
        ),
    )

    val ALL: List<RecyclingPoint> = buildList {
        addAll(pevs)
        addAll(ecopontosComlurb)
        addAll(ecopontosLight)
    }
}
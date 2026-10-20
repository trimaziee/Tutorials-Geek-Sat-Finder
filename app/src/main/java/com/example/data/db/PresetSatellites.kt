package com.example.data.db

import com.example.data.model.Satellite
import com.example.data.model.Transponder

object PresetSatellites {

    val PRESET_LIST: List<Satellite> = listOf(
        Satellite(
            id = 1,
            name = "PakSat 1R / MM1",
            orbitalPosition = 38.0,
            band = "C-Band",
            defaultLnbLo = 5150,
            recommendedDishSize = "5ft – 8ft Prime Focus",
            popularChannels = "PTV Home, PTV Sports HD, Hum News, Geo News, ARY Digital, Express, Dunya, Samaa",
            strongTransponder = "4158 V 12000",
            transponders = listOf(
                Transponder(4158, "V", 12000, "3/4", "DVB-S2", true, "PTV National / Sports Mux (Strongest)"),
                Transponder(4054, "V", 7200, "3/4", "DVB-S2", false, "Geo Network Mux"),
                Transponder(4170, "V", 5000, "3/4", "DVB-S2", false, "Dunya News"),
                Transponder(3813, "V", 7400, "3/4", "DVB-S2", false, "ARY Mux"),
                Transponder(4180, "V", 6000, "3/4", "DVB-S2", false, "Samaa TV")
            ),
            coverageNotes = "Primary national satellite for Pakistan. Strongest beam across Pakistan, Afghanistan, Middle East.",
            isFavorite = true
        ),
        Satellite(
            id = 2,
            name = "AsiaSat 7",
            orbitalPosition = 105.5,
            band = "C-Band",
            defaultLnbLo = 5150,
            recommendedDishSize = "5ft – 8ft Prime Focus",
            popularChannels = "ARY News, ARY Zauq, Express News, Madani Channel, Peace TV, Sahar TV, Hum TV",
            strongTransponder = "4065 H 4296",
            transponders = listOf(
                Transponder(4065, "H", 4296, "3/4", "DVB-S", true, "Madani Channel (Benchmark Tracking TP)"),
                Transponder(3915, "V", 7200, "3/4", "DVB-S2", false, "Hum TV Mux"),
                Transponder(4040, "H", 29720, "3/4", "DVB-S", false, "ARY Network Package"),
                Transponder(3880, "H", 27500, "3/4", "DVB-S2", false, "Express Media Mux"),
                Transponder(3760, "H", 26000, "3/4", "DVB-S2", false, "Phoenix Satellite Mux")
            ),
            coverageNotes = "Extremely popular in South Asia. Very strong C-band signal across South Asia, East Asia, and Middle East.",
            isFavorite = true
        ),
        Satellite(
            id = 3,
            name = "GSAT 15 / DD FreeDish",
            orbitalPosition = 93.5,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2ft – 3ft (60cm – 90cm) Offset",
            popularChannels = "DD Sports, Zee Anmol, Star Utsav, Sony Pal, Colors Rishtey, Goldmines, B4U",
            strongTransponder = "11090 V 29500",
            transponders = listOf(
                Transponder(11090, "V", 29500, "3/4", "DVB-S", true, "DD FreeDish National (Highest Signal)"),
                Transponder(11170, "V", 29500, "3/4", "DVB-S", false, "DD National / Bharti Mux"),
                Transponder(11470, "V", 29500, "3/4", "DVB-S", false, "Doordarshan FTA Channels"),
                Transponder(11510, "V", 29500, "3/4", "DVB-S", false, "Regional FTA Package"),
                Transponder(11550, "V", 29500, "3/4", "DVB-S", false, "MPEG-4 Mux")
            ),
            coverageNotes = "Over 100 Free-To-Air channels. Works on small 2ft dish across Pakistan and India with zero monthly fees.",
            isFavorite = true
        ),
        Satellite(
            id = 4,
            name = "NSS 6 / SES 8",
            orbitalPosition = 95.0,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2ft (60cm) Offset",
            popularChannels = "Dish TV Package, Zee Cinema, News Channels, South Asian Entertainment",
            strongTransponder = "12688 V 27500",
            transponders = listOf(
                Transponder(12688, "V", 27500, "3/4", "DVB-S", true, "Dish TV Info TP"),
                Transponder(12535, "V", 40700, "3/4", "DVB-S2", false, "SES 8 Ku Transponder"),
                Transponder(12647, "H", 32700, "3/4", "DVB-S2", false, "Dish TV India Ku Mux")
            ),
            coverageNotes = "Co-located very close to 93.5E DD FreeDish; often received on the same Ku LNB!",
            isFavorite = true
        ),
        Satellite(
            id = 5,
            name = "Yahsat 1A / Al Yah 1",
            orbitalPosition = 52.5,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2ft – 3ft (60cm – 90cm) Offset",
            popularChannels = "Watan HD, Ariana TV, Varzesh TV (Football), Persiana, PMC, A1 Balkan, GEM TV",
            strongTransponder = "11843 V 27500",
            transponders = listOf(
                Transponder(11843, "V", 27500, "5/6", "DVB-S", true, "East Beam Persiana Package (Strongest)"),
                Transponder(12015, "H", 27500, "3/4", "DVB-S2", true, "Watan HD / Ariana TV (Live Cricket/Football)"),
                Transponder(11938, "H", 27500, "5/6", "DVB-S", false, "GEM TV Package"),
                Transponder(12149, "V", 27500, "3/4", "DVB-S2", false, "YahLive Mux")
            ),
            coverageNotes = "Extremely popular for live sports and movie FTA channels across Pakistan, Afghanistan, Iran, and UAE.",
            isFavorite = true
        ),
        Satellite(
            id = 6,
            name = "Express AM6 / TürkmenÄlem",
            orbitalPosition = 53.0,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2ft (60cm) Offset",
            popularChannels = "Persian Movie Channels, Radio Javan HD, Turkmen TV, Russian Channels",
            strongTransponder = "10887 H 27500",
            transponders = listOf(
                Transponder(10887, "H", 27500, "2/3", "DVB-S", true, "Wide Beam Persian Mux"),
                Transponder(12226, "V", 28000, "5/6", "DVB-S", false, "Turkmenistan Package")
            ),
            coverageNotes = "Located right next to Yahsat 52.5°E; both are received simultaneously on a single Ku LNB.",
            isFavorite = false
        ),
        Satellite(
            id = 7,
            name = "Hotbird 13G / 13F",
            orbitalPosition = 13.0,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2.5ft – 4ft (80cm – 120cm) Offset",
            popularChannels = "BBC World, Rai 1/2/3, TVP Polonia, EuroNews, Al Jazeera, RTL, TV5 Monde",
            strongTransponder = "11034 V 27500",
            transponders = listOf(
                Transponder(11034, "V", 27500, "3/4", "DVB-S", true, "Eutelsat Promo / Free Info"),
                Transponder(10992, "V", 27500, "2/3", "DVB-S", false, "RAI Mux"),
                Transponder(11137, "H", 27500, "3/4", "DVB-S", false, "Bloomberg Europe"),
                Transponder(11565, "H", 29900, "3/4", "DVB-S2", false, "Polski Mux")
            ),
            coverageNotes = "Major European hub. Hundreds of international news, culture, and entertainment channels.",
            isFavorite = true
        ),
        Satellite(
            id = 8,
            name = "Türksat 42E (3A/4A/5B)",
            orbitalPosition = 42.0,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2.5ft – 3ft Offset",
            popularChannels = "TRT 1, TRT Spor HD, ATV Turkey, Show TV, Star TV, Kanal D, HaberTurk",
            strongTransponder = "12380 V 27500",
            transponders = listOf(
                Transponder(12380, "V", 27500, "3/4", "DVB-S", true, "West Beam Package"),
                Transponder(12054, "H", 27500, "5/6", "DVB-S", false, "TRT HD Package"),
                Transponder(11958, "V", 27500, "5/6", "DVB-S", false, "Kanal D / Star TV")
            ),
            coverageNotes = "Popular for Turkish drama series and UEFA sports. Strong coverage across Europe and Turkey/Central Asia.",
            isFavorite = false
        ),
        Satellite(
            id = 9,
            name = "Intelsat 20 (IS-20)",
            orbitalPosition = 68.5,
            band = "Dual C/Ku",
            defaultLnbLo = 5150,
            recommendedDishSize = "6ft Prime Focus (C) / 3ft (Ku)",
            popularChannels = "South African Channels, Christian Channels, Sony Network India, Star India Feed",
            strongTransponder = "4015 V 30000",
            transponders = listOf(
                Transponder(4015, "V", 30000, "3/4", "DVB-S2", true, "C-Band Sony Package"),
                Transponder(12682, "H", 30000, "3/4", "DVB-S2", false, "Ku-Band South Africa Feed"),
                Transponder(3740, "H", 30000, "3/4", "DVB-S2", false, "C-Band Star India Feeds")
            ),
            coverageNotes = "Key satellite for broadcasters in India and Southern Africa.",
            isFavorite = false
        ),
        Satellite(
            id = 10,
            name = "Measat 3a / 3b / 3d",
            orbitalPosition = 91.5,
            band = "Dual C/Ku",
            defaultLnbLo = 5150,
            recommendedDishSize = "6ft (C) / 3ft (Ku)",
            popularChannels = "Astro Malaysia, TV1, TV2, Bernama, Discovery Asia, HBO HD Asia Feeds",
            strongTransponder = "3880 V 29720",
            transponders = listOf(
                Transponder(3880, "V", 29720, "3/4", "DVB-S2", true, "Measat Global C-Band TP"),
                Transponder(12224, "V", 30000, "3/4", "DVB-S2", false, "Ku-Band Astro Package")
            ),
            coverageNotes = "South-East Asia and South Asia footprint.",
            isFavorite = false
        ),
        Satellite(
            id = 11,
            name = "Badr 4 / 5 / 6 / 7 (Arabsat)",
            orbitalPosition = 26.0,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2.5ft – 3ft Offset",
            popularChannels = "MBC 1/2/3/4/Action, Al Arabiya, Dubai One, Saudi Quran, Rotana Cinema",
            strongTransponder = "11919 H 27500",
            transponders = listOf(
                Transponder(11919, "H", 27500, "3/4", "DVB-S", true, "MBC Group HD Package (Strongest)"),
                Transponder(12130, "V", 27500, "3/4", "DVB-S", false, "Dubai Media Network"),
                Transponder(12284, "V", 27500, "3/4", "DVB-S", false, "Saudi TV HD Package")
            ),
            coverageNotes = "Primary satellite for Middle Eastern and Arab world broadcast.",
            isFavorite = false
        ),
        Satellite(
            id = 12,
            name = "Nilesat 201 / 301",
            orbitalPosition = -7.0, // 7.0° West
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "3ft – 4ft Offset",
            popularChannels = "Al Jazeera, Nile TV, MBC Egypt, DMC, ON TV, Melody, Rotana Classic",
            strongTransponder = "11747 V 27500",
            transponders = listOf(
                Transponder(11747, "V", 27500, "5/6", "DVB-S", true, "Nilesat Promo Mux"),
                Transponder(11766, "H", 27500, "5/6", "DVB-S", false, "Al Jazeera Mux")
            ),
            coverageNotes = "Major broadcast position for North Africa and the Arab World.",
            isFavorite = false
        ),
        Satellite(
            id = 13,
            name = "Astra 19.2E (1KR/1L/1M/1N)",
            orbitalPosition = 19.2,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2ft – 3ft (60cm – 90cm) Offset",
            popularChannels = "ZDF HD, Das Erste HD, RTL, ProSieben, Servus TV, Eurosport 1 Deutschland",
            strongTransponder = "11836 H 27500",
            transponders = listOf(
                Transponder(11836, "H", 27500, "3/4", "DVB-S", true, "ARD Digital Mux (Strongest European TP)"),
                Transponder(11954, "H", 27500, "3/4", "DVB-S", false, "ZDF Vision Mux"),
                Transponder(12226, "H", 27500, "3/4", "DVB-S", false, "RTL Television Group")
            ),
            coverageNotes = "The flagship satellite for Western and Central Europe.",
            isFavorite = false
        ),
        Satellite(
            id = 14,
            name = "Astra 28.2E (2E/2F/2G)",
            orbitalPosition = 28.2,
            band = "Ku-Band",
            defaultLnbLo = 9750,
            recommendedDishSize = "2ft Offset (UK) / 3ft+ (Europe)",
            popularChannels = "BBC One/Two, ITV 1, Channel 4, Sky News FTA, Film4, CBS Reality",
            strongTransponder = "11778 V 27500",
            transponders = listOf(
                Transponder(11778, "V", 27500, "3/4", "DVB-S", true, "Sky Default Guide Transponder"),
                Transponder(10773, "H", 22000, "5/6", "DVB-S", false, "BBC One London HD")
            ),
            coverageNotes = "UK FreeSat and British broadcast position.",
            isFavorite = false
        ),
        Satellite(
            id = 15,
            name = "Apstar 7",
            orbitalPosition = 76.5,
            band = "C-Band",
            defaultLnbLo = 5150,
            recommendedDishSize = "6ft Prime Focus",
            popularChannels = "Bangla Vision, Channel i, BTV National, Sony Ten 1 Feed, Zee TV Feed",
            strongTransponder = "3780 V 30000",
            transponders = listOf(
                Transponder(3780, "V", 30000, "3/4", "DVB-S2", true, "Bangladesh Mux (High Signal)"),
                Transponder(4030, "V", 6312, "3/4", "DVB-S2", false, "BTV National Feed")
            ),
            coverageNotes = "Popular across South Asia for Bangladeshi channels and live sports downlinks.",
            isFavorite = false
        )
    )
}

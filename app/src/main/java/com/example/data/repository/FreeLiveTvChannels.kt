package com.example.data.repository

import com.example.data.model.LiveTvChannelDto
import com.example.data.model.LiveTvProgramDto

object FreeLiveTvChannels {
    val channels: List<LiveTvChannelDto> = listOf(
        // ==================== IOWA & CEDAR FALLS LIVE FEEDS & IA511 CAMERAS ====================
        LiveTvChannelDto(
            id = "ia511_cf_us218_ia57",
            name = "IA511: Cedar Falls - US-218 & IA-57",
            number = "01",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Cedar Falls, IA",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_84-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_84-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_84-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live Highway Cam • US-218 & IA-57 (1st St)",
                overview = "Iowa DOT Traffic Camera overlooking US-218 & IA-57 interchange in North Cedar Falls and Cedar River corridor.",
                genres = listOf("Traffic", "Live Cam", "Cedar Falls")
            ),
            weatherNote = "Flow: Normal • Roadway: Dry"
        ),
        LiveTvChannelDto(
            id = "ia511_cf_us218_viking",
            name = "IA511: Cedar Falls - US-218 & Viking Rd",
            number = "02",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Cedar Falls, IA",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_20-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_20-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_20-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live Traffic Cam • US-218 & Viking Rd Plaza",
                overview = "Iowa DOT Live Camera monitoring Viking Rd, Target commercial corridor, and Cedar Falls Industrial Park.",
                genres = listOf("Traffic", "Live Cam", "Cedar Falls")
            ),
            weatherNote = "Flow: Normal • Visibility: High"
        ),
        LiveTvChannelDto(
            id = "ia511_cf_ia58_university",
            name = "IA511: Cedar Falls - IA-58 & University Ave",
            number = "03",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Cedar Falls, IA",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_10-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_10-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_10-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live Traffic Cam • IA-58 & University Ave (UNI)",
                overview = "Iowa DOT Live Camera at IA-58 & University Ave gateway to University of Northern Iowa (UNI Panthers) and College Hill.",
                genres = listOf("Traffic", "Live Cam", "Cedar Falls")
            ),
            weatherNote = "Flow: Flowing • Weather: Clear"
        ),
        LiveTvChannelDto(
            id = "ia511_cf_us20_hudson",
            name = "IA511: Cedar Falls - US-20 & Hudson Rd",
            number = "04",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Cedar Falls, IA",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_11-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_11-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_11-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live Highway Cam • US-20 & Hudson Rd South Bypass",
                overview = "Iowa DOT Live Camera on US Highway 20 and Hudson Road southern bypass interchange.",
                genres = listOf("Traffic", "Live Cam", "Cedar Falls")
            ),
            weatherNote = "Flow: Normal • Speed: 65 mph"
        ),
        LiveTvChannelDto(
            id = "ia511_wloo_us20_us218",
            name = "IA511: Waterloo - US-20 & US-218 Interchange",
            number = "05",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Waterloo, IA",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_13-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_13-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_13-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live Junction Cam • US-20 / US-218 Black Hawk Hub",
                overview = "Primary Black Hawk County transit junction linking Waterloo, Cedar Falls, and Evansdale.",
                genres = listOf("Traffic", "Live Cam", "Waterloo")
            ),
            weatherNote = "Flow: Moderate • Roadway: Clear"
        ),
        LiveTvChannelDto(
            id = "ia511_wloo_i380_sanmarnan",
            name = "IA511: Waterloo - I-380 & San Marnan Dr",
            number = "06",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Waterloo, IA",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_15-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_15-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_15-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live Traffic Cam • I-380 MM 72.4 (Crossroads)",
                overview = "Iowa DOT Camera on Interstate 380 Mile Marker 72.4 adjacent to San Marnan Drive and Crossroads Center in South Waterloo.",
                genres = listOf("Traffic", "Live Cam", "Waterloo")
            ),
            weatherNote = "Flow: Normal • Speed: 60 mph"
        ),
        LiveTvChannelDto(
            id = "ia511_wloo_airport",
            name = "IA511: Waterloo - US-218 & Airport Blvd",
            number = "07",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Waterloo, IA",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_25-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_25-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_25-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live SkyCam • Waterloo Regional Airport & US-218",
                overview = "Live traffic and runway approach camera overlooking Waterloo Regional Airport (ALO) and Highway 218 entrance.",
                genres = listOf("Traffic", "Live Cam", "Airport")
            ),
            weatherNote = "Runway & Roads: Good"
        ),
        LiveTvChannelDto(
            id = "ia511_i80_i35_mixmaster",
            name = "IA511: Des Moines - I-80 & I-35 Interchange",
            number = "08",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            isTrafficCam = true,
            location = "Central Iowa",
            streamUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_90-01.jpg",
            snapshotUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_90-01.jpg",
            logoUrl = "https://atmsqf.iowadot.gov/snapshots/Public/RWIS/RWIS_90-01.jpg",
            currentProgram = LiveTvProgramDto(
                name = "Live Highway Cam • I-80 / I-35 Major Iowa Crossroads",
                overview = "Central Iowa primary cross-country freight intersection connecting Council Bluffs, Des Moines, and Minneapolis.",
                genres = listOf("Traffic", "Live Cam", "Des Moines")
            ),
            weatherNote = "Flow: Heavy Freight • Flowing"
        ),
        LiveTvChannelDto(
            id = "free_iowa_news_radar",
            name = "Eastern Iowa Live & Doppler Radar",
            number = "10",
            category = "Iowa & Cedar Falls",
            isOnlineFast = true,
            location = "Cedar Falls & Waterloo, IA",
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1590556409324-aa1d726e5c3c?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Cedar Valley Live News & Severe Weather Radar",
                overview = "Live local news coverage, Cedar Valley breaking updates, and live Doppler radar for Cedar Falls, Waterloo, Cedar Rapids, and Dubuque.",
                genres = listOf("Local News", "Iowa", "Weather")
            )
        ),

        // ==================== CLASSIC CARTOONS & VINTAGE ANIMATION ====================
        LiveTvChannelDto(
            id = "free_toonami_aftermath",
            name = "Toonami Aftermath (East)",
            number = "21",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = "http://api.toonamiaftermath.com:3000/est/playlist.m3u8",
            logoUrl = "https://i.imgur.com/aSjhZK7.png",
            currentProgram = LiveTvProgramDto(
                name = "Toonami 90s/00s Classic Broadcast Block (East)",
                overview = "The official fan-favorite 24/7 retro broadcast of classic anime (Dragon Ball Z, Gundam Wing, Sailor Moon, Yu Yu Hakusho, Outlaw Star) with Tom, Clyde 49, vintage Moltar bumps, and retro promos.",
                genres = listOf("Toonami", "Anime", "Action")
            )
        ),
        LiveTvChannelDto(
            id = "free_toonami_west",
            name = "Toonami Aftermath (West)",
            number = "22",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = "http://api.toonamiaftermath.com:3000/pst/playlist.m3u8",
            logoUrl = "https://i.imgur.com/aSjhZK7.png",
            currentProgram = LiveTvProgramDto(
                name = "Toonami West Coast Feed",
                overview = "24/7 continuous retro Toonami schedule delayed for Pacific Standard Time viewers, featuring Rurouni Kenshin, Tenchi Muyo, and classic Cartoon Network blocks.",
                genres = listOf("Toonami", "Anime", "Retro")
            )
        ),
        LiveTvChannelDto(
            id = "free_toonami_movies",
            name = "Toonami Aftermath Movies",
            number = "23",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = "http://api.toonamiaftermath.com:3000/movies/playlist.m3u8",
            logoUrl = "https://i.imgur.com/aSjhZK7.png",
            currentProgram = LiveTvProgramDto(
                name = "Toonami Animated Feature Films",
                overview = "Feature-length anime movies, Dragon Ball Z theatrical films, Akira, Ghost in the Shell, and classic animated cinema marathons.",
                genres = listOf("Anime", "Movies", "Toonami")
            )
        ),
        LiveTvChannelDto(
            id = "free_toonami_snick",
            name = "Toonami SNICK (90s Block)",
            number = "24",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = "http://api.toonamiaftermath.com:3000/est/playlist.m3u8",
            logoUrl = "https://i.imgur.com/aSjhZK7.png",
            currentProgram = LiveTvProgramDto(
                name = "90s Saturday Night SNICK & Animation",
                overview = "Classic 90s nostalgia broadcast with vintage Nickelodeon programming, Rugrats, Doug, Rocko's Modern Life, and retro commercial breaks.",
                genres = listOf("Classic Cartoons", "Retro", "Nostalgia")
            )
        ),
        LiveTvChannelDto(
            id = "free_toonami_radio",
            name = "Toonami Aftermath Radio",
            number = "25",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = "http://api.toonamiaftermath.com:3000/radio/playlist.m3u8",
            logoUrl = "https://i.imgur.com/aSjhZK7.png",
            currentProgram = LiveTvProgramDto(
                name = "Toonami Radio: Space Beats & Vintage Promos",
                overview = "24/7 continuous stream of classic Toonami soundtrack grooves, ambient space hip-hop, Joe Boyd Vigil beats, and vintage Peter Cullen voiceovers.",
                genres = listOf("Toonami", "Radio", "Beats", "Soundtrack")
            )
        ),
        LiveTvChannelDto(
            id = "free_liquid_tv",
            name = "MTV Liquid Television",
            number = "26",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = LiquidTvCatalog.getCurrentScheduledEpisode().streamUrl,
            logoUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = LiquidTvCatalog.getCurrentScheduledEpisode().fullTitle,
                overview = "MTV's revolutionary 90s animation anthology showcasing the debut of Æon Flux, Beavis & Butt-Head, Winter Steele, The Running Man, Stick Figure Theater, and underground indie shorts: ${LiquidTvCatalog.getCurrentScheduledEpisode().featuredSegments}",
                genres = listOf("Animation", "90s", "Underground", "MTV"),
                blockName = "Liquid TV"
            ),
            upcomingPrograms = LiquidTvCatalog.getUpcomingSchedule()
        ),
        LiveTvChannelDto(
            id = "free_cartoon_classics",
            name = "Cartoon Classics 24/7",
            number = "27",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = "https://daiconnect.com/live/hls/tvup/rk-cartoonclassics/578f4b7eb725168349ec0af81b21d388/index.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Popeye, Betty Boop & Merrie Melodies Marathon",
                overview = "Golden Age classic cartoons in full color: Popeye the Sailor, Fleischer Studios Superman, Betty Boop, Felix the Cat, and Casper.",
                genres = listOf("Classic Cartoons", "Vintage", "Animation")
            )
        ),
        LiveTvChannelDto(
            id = "free_buzzr",
            name = "Buzzr TV (Retro Game Shows)",
            number = "28",
            category = "Classic Cartoons",
            isOnlineFast = true,
            streamUrl = "http://23.237.104.106:8080/USA_BUZZR/index.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d6/Buzzr_logo.svg/960px-Buzzr_logo.svg.png",
            currentProgram = LiveTvProgramDto(
                name = "Vintage Game Show Marathons",
                overview = "The iconic vintage American game show channel featuring Match Game, Family Feud, Supermarket Sweep, Password, and Card Sharks.",
                genres = listOf("Retro", "Game Shows", "Classics", "Nostalgia")
            )
        ),

        // ==================== ANIME 24/7 ====================
        LiveTvChannelDto(
            id = "free_aniplus_asia",
            name = "Aniplus 24/7 Anime",
            number = "31",
            category = "Anime",
            isOnlineFast = true,
            streamUrl = "https://amg18481-amg18481c1-amgplt0352.playout.now3.amagi.tv/playlist/amg18481-amg18481c1-amgplt0352/playlist.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "24/7 Shonen & Cyberpunk Action Series",
                overview = "Continuous Japanese anime broadcast featuring high-octane battles, mecha duels, fantasy adventures, and cult anime OVA series.",
                genres = listOf("Anime", "Action", "Japanese")
            )
        ),
        LiveTvChannelDto(
            id = "free_toonami_action_block",
            name = "Toonami Midnight Run",
            number = "32",
            category = "Anime",
            isOnlineFast = true,
            streamUrl = "http://api.toonamiaftermath.com:3000/est/playlist.m3u8",
            logoUrl = "https://i.imgur.com/aSjhZK7.png",
            currentProgram = LiveTvProgramDto(
                name = "Midnight Run Uncut Anime Block",
                overview = "Uncut late-night anime block featuring Cowboy Bebop, Trigun, The Big O, and FLCL with ambient space bumps.",
                genres = listOf("Anime", "Toonami", "Retro")
            )
        ),

        // ==================== NEWS & WEATHER ====================
        LiveTvChannelDto(
            id = "free_weathernation",
            name = "WeatherNation Live",
            number = "41",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1504608524841-42fe6f032b4b?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Midwest Storm Tracker & 24/7 Radar",
                overview = "Continuous national and Midwest weather radar coverage, tornado watches, freeze advisories, and live storm chasing.",
                genres = listOf("Weather", "Radar", "Midwest")
            )
        ),
        LiveTvChannelDto(
            id = "free_livenow_fox",
            name = "LiveNOW from FOX",
            number = "42",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "LiveNOW Breaking News & Live Event Feeds",
                overview = "Raw, unfiltered live breaking news coverage from across the United States, live press conferences, and regional updates.",
                genres = listOf("News", "Breaking", "Live")
            )
        ),
        LiveTvChannelDto(
            id = "free_dw_news",
            name = "DW News English",
            number = "43",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "DW Global News Live",
                overview = "In-depth international news reporting, business insights, and current geopolitical affairs from Berlin.",
                genres = listOf("News", "World")
            )
        ),
        LiveTvChannelDto(
            id = "free_euronews",
            name = "Euronews International",
            number = "44",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Euronews Live Rolling News",
                overview = "Continuous unbiased European and global news headlines, culture, and economic developments.",
                genres = listOf("News", "Politics")
            )
        ),
        LiveTvChannelDto(
            id = "free_nasa_tv",
            name = "NASA TV HD",
            number = "45",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "ISS Live Stream & Artemis Deep Space Updates",
                overview = "Live coverage of missions, astronauts aboard the International Space Station, and scientific discoveries.",
                genres = listOf("Science", "Documentary")
            )
        ),
        LiveTvChannelDto(
            id = "free_court_tv",
            name = "Court TV Live",
            number = "46",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://content.uplynk.com/channel/6c0bd0f94b1d4526a98676e9699a10ef.m3u8",
            logoUrl = "https://graph.facebook.com/courttv/picture?width=200&height=200",
            currentProgram = LiveTvProgramDto(
                name = "Live Courtroom Coverage & Legal Trials",
                overview = "Gavel-to-gavel live courtroom coverage of high-profile trials, expert analysis, and legal commentary.",
                genres = listOf("True Crime", "Court", "Legal", "News")
            )
        ),
        LiveTvChannelDto(
            id = "free_fox_weather",
            name = "Fox Weather 24/7",
            number = "47",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://247wlive.foxweather.com/stream/index.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Fox_Weather_logo.svg/500px-Fox_Weather_logo.svg.png",
            currentProgram = LiveTvProgramDto(
                name = "Live Weather Radar & Severe Storm Alerts",
                overview = "24/7 live meteorologist updates, live 3D Doppler radar, storm chasing coverage, and national forecasts.",
                genres = listOf("Weather", "Radar", "Live", "Storms")
            )
        ),
        LiveTvChannelDto(
            id = "free_gb_news",
            name = "GB News Live (UK)",
            number = "48",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://live-gbnews.simplestreamcdn.com/live5/gbnews/bitrate1.isml/manifest.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/3/35/GB_News_Logo.svg/500px-GB_News_Logo.svg.png",
            currentProgram = LiveTvProgramDto(
                name = "Live UK Breaking News & Debate",
                overview = "24-hour British television news channel with rolling national headlines, parliamentary reporting, interviews, and debate.",
                genres = listOf("News", "UK", "Debate", "Current Affairs")
            )
        ),
        LiveTvChannelDto(
            id = "free_bloomberg_tv",
            name = "Bloomberg Television Live",
            number = "49",
            category = "News & Weather",
            isOnlineFast = true,
            streamUrl = "https://bloomberg.com/media-manifest/streams/us.m3u8",
            logoUrl = "https://i.imgur.com/VnCcH73.png",
            currentProgram = LiveTvProgramDto(
                name = "Bloomberg Markets: Global Open",
                overview = "The global financial authority delivering live stock market quotes, Wall Street analysis, economic trends, and technology news.",
                genres = listOf("Business", "Finance", "Markets", "News")
            )
        ),

        // ==================== SPORTS ====================
        LiveTvChannelDto(
            id = "free_redbull_tv",
            name = "Red Bull TV",
            number = "51",
            category = "Sports",
            isOnlineFast = true,
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Extreme Sports & Downhill Mountain Biking Championship",
                overview = "World-class adrenaline sports, motorsports, skateboarding, snowboarding and athlete stories.",
                genres = listOf("Sports", "Action")
            )
        ),
        // ==================== ENTERTAINMENT & MOVIES ====================
        LiveTvChannelDto(
            id = "free_classic_movies",
            name = "30A Classic Cinema 24/7",
            number = "61",
            category = "Movies & TV",
            isOnlineFast = true,
            streamUrl = "https://30a-tv.com/feeds/pzaz/30atvmovies.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Golden Age Cinema & Feature Films",
                overview = "24/7 vintage Hollywood feature films, classic mystery thrillers, film noir, and cinematic classics.",
                genres = listOf("Movies", "Cinema", "Classics")
            )
        ),
        LiveTvChannelDto(
            id = "free_great_movies_uk",
            name = "Great! Movies (UK)",
            number = "62",
            category = "Movies & TV",
            isOnlineFast = true,
            streamUrl = "https://amg01753-narrativeuk-amg01753c3-lg-gb-1833.playouts.now.amagi.tv/playlist/amg01753-narrativeuk-greatmovies-lggb/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/9/92/Great%21_Movies_logo_2021.svg/500px-Great%21_Movies_logo_2021.svg.png",
            currentProgram = LiveTvProgramDto(
                name = "Great! Hollywood & British Cinema 24/7",
                overview = "UK free-to-air cinema channel bringing big Hollywood blockbusters, award-winning dramas, romantic comedies, and classic modern features.",
                genres = listOf("Movies", "UK", "Cinema", "Drama")
            )
        ),
        LiveTvChannelDto(
            id = "free_action_hollywood",
            name = "Action Hollywood Movies",
            number = "63",
            category = "Movies & TV",
            isOnlineFast = true,
            streamUrl = "https://cdn-apse1-prod.tsv2.amagi.tv/linear/amg01076-lightningintern-actionhollywood-samsungnz/playlist.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "High-Octane Action & Thriller Cinema",
                overview = "Non-stop pulse-pounding Hollywood action, martial arts classics, crime thrillers, and explosive stunt-driven features.",
                genres = listOf("Action", "Movies", "Thrillers")
            )
        ),

        // ==================== NATURE & WILDLIFE ====================
        LiveTvChannelDto(
            id = "free_naturevision",
            name = "NatureVision Wildlife TV",
            number = "71",
            category = "Nature & Docs",
            isOnlineFast = true,
            streamUrl = "https://playertest.longtailvideo.com/adaptive/oceans/oceans.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Serene Wilderness & Natural Wonders",
                overview = "Stunning footage of oceans, alpine forests, waterfalls, and exotic animal habitats worldwide.",
                genres = listOf("Nature", "Documentary")
            )
        ),

        // ==================== KIDS & FAMILY ====================
        LiveTvChannelDto(
            id = "free_kids_animation",
            name = "Kids Animation Live",
            number = "81",
            category = "Kids",
            isOnlineFast = true,
            streamUrl = "https://3abn.bozztv.com/3abn2/Kids_live/smil:Kids_live.smil/playlist.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1566140967404-b8b3932483f5?w=400&q=80",
            currentProgram = LiveTvProgramDto(
                name = "Adventures & Animated Storybook Explorers",
                overview = "Wholesome, colorful animated stories, music, and learning shows tailored for children and families.",
                genres = listOf("Kids", "Animation", "Family")
            )
        ),
        LiveTvChannelDto(
            id = "free_pop_kids_uk",
            name = "Pop TV (Kids & Cartoons)",
            number = "82",
            category = "Kids",
            isOnlineFast = true,
            streamUrl = "https://amg01753-narrativeentert-popkids-lggb-xyy5k.amagi.tv/ts-eu-w1-n2/playlist/amg01753-narrativeentert-popkids-lggb/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/3/36/Pop_UK_TV_Logo_%282015%29.svg/500px-Pop_UK_TV_Logo_%282015%29.svg.png",
            currentProgram = LiveTvProgramDto(
                name = "Popular Animated Series & Adventures",
                overview = "The UK's premier commercial children's channel featuring top animation, Pokémon, Miraculous, Alvinnn!!!, and vibrant cartoons.",
                genres = listOf("Kids", "Cartoons", "Animation", "Family")
            )
        ),

        // ==================== MUSIC & RADIO ====================
        LiveTvChannelDto(
            id = "free_now_80s",
            name = "Now 80s (Retro Music TV)",
            number = "91",
            category = "Music & Radio",
            isOnlineFast = true,
            streamUrl = "https://lightning-now80s-samsunguk.amagi.tv/playlist.m3u8",
            logoUrl = "https://i.imgur.com/8paz37m.png",
            currentProgram = LiveTvProgramDto(
                name = "80s Synth-Pop & New Wave Party",
                overview = "Non-stop vintage 1980s music videos: Madonna, Michael Jackson, Duran Duran, Prince, Wham!, Depeche Mode, and classic hits.",
                genres = listOf("Music", "80s", "Retro", "Pop")
            )
        ),
        LiveTvChannelDto(
            id = "free_now_rock",
            name = "Now Rock (Classic & Modern Rock)",
            number = "92",
            category = "Music & Radio",
            isOnlineFast = true,
            streamUrl = "https://lightning-now90s-samsungnz.amagi.tv/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/8/89/NOW_Rock_logo.png",
            currentProgram = LiveTvProgramDto(
                name = "Rock Anthems & Legends",
                overview = "24/7 stadium rock, grunge, 90s alternative, metal, and guitar anthems from Queen, Nirvana, Foo Fighters, and Led Zeppelin.",
                genres = listOf("Music", "Rock", "Alternative", "Legends")
            )
        )
    )
}

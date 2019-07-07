package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.enums;


import org.codehaus.mojo.jaxb2.shared.Validate;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>Enumeration of Swedish Municipalities. The enumeration name of each municipality is synthesized from the
 * lower-case name of the municipality by replacing each scandinavian character with the corresponding ASCII
 * character and replacing whitespace and punctuation with underscore. For example, this implies that "Järfälla"
 * municipality has the enum constant "jarfalla", and that "Östra Göinge" has the enum constant "ostra_goinge".</p>
 * <p>A special case exists for the two municipalities "Håbo" in Uppsala county and "Habo" in Jönköping county.
 * Since the enum constant for both these municipalities would be "habo", the county name is appended yielding the
 * two constants {@code habo_uppsala} and {@code habo_jonkoping}.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "http://sweden.is.amazing")
@XmlEnum
@XmlSeeAlso(County.class)
public enum Municipality implements Serializable {

    // Stockholm municipalities
    @XmlEnumValue("0114")upplands_vasby(1, 114, "Upplands Väsby"),
    @XmlEnumValue("0115")vallentuna(1, 115, "Vallentuna"),
    @XmlEnumValue("0117")osteraker(1, 117, "Österåker"),
    @XmlEnumValue("0120")varmdo(1, 120, "Värmdö"),
    @XmlEnumValue("0123")jarfalla(1, 123, "Järfälla"),
    @XmlEnumValue("0125")ekero(1, 125, "Ekerö"),
    @XmlEnumValue("0126")huddinge(1, 126, "Huddinge"),
    @XmlEnumValue("0127")botkyrka(1, 127, "Botkyrka"),
    @XmlEnumValue("0128")salem(1, 128, "Salem"),
    @XmlEnumValue("0136")haninge(1, 136, "Haninge"),
    @XmlEnumValue("0138")tyreso(1, 138, "Tyresö"),
    @XmlEnumValue("0139")upplands_bro(1, 139, "Upplands-Bro"),
    @XmlEnumValue("0140")nykvarn(1, 140, "Nykvarn"),
    @XmlEnumValue("0160")taby(1, 160, "Täby"),
    @XmlEnumValue("0162")danderyd(1, 162, "Danderyd"),
    @XmlEnumValue("0163")sollentuna(1, 163, "Sollentuna"),
    @XmlEnumValue("0180")stockholm(1, 180, "Stockholm"),
    @XmlEnumValue("0181")sodertalje(1, 181, "Södertälje"),
    @XmlEnumValue("0182")nacka(1, 182, "Nacka"),
    @XmlEnumValue("0183")sundbyberg(1, 183, "Sundbyberg"),
    @XmlEnumValue("0184")solna(1, 184, "Solna"),
    @XmlEnumValue("0186")lidingo(1, 186, "Lidingö"),
    @XmlEnumValue("0187")vaxholm(1, 187, "Vaxholm"),
    @XmlEnumValue("0188")norrtalje(1, 188, "Norrtälje"),
    @XmlEnumValue("0191")sigtuna(1, 191, "Sigtuna"),
    @XmlEnumValue("0192")nynashamn(1, 192, "Nynäshamn"),

    // Uppsala municipalities
    @XmlEnumValue("0305")habo_uppsala(3, 305, "Håbo"),
    @XmlEnumValue("0319")alvkarleby(3, 319, "Älvkarleby"),
    @XmlEnumValue("0330")knivsta(3, 330, "Knivsta"),
    @XmlEnumValue("0331")heby(3, 331, "Heby"),
    @XmlEnumValue("0360")tierp(3, 360, "Tierp"),
    @XmlEnumValue("0380")uppsala(3, 380, "Uppsala"),
    @XmlEnumValue("0381")enkoping(3, 381, "Enköping"),
    @XmlEnumValue("0382")osthammar(3, 382, "Östhammar"),

    // Södermanland municipalities
    @XmlEnumValue("0428")vingaker(4, 428, "Vingåker"),
    @XmlEnumValue("0461")gnesta(4, 461, "Gnesta"),
    @XmlEnumValue("0480")nykoping(4, 480, "Nyköping"),
    @XmlEnumValue("0481")oxelosund(4, 481, "Oxelösund"),
    @XmlEnumValue("0482")flen(4, 482, "Flen"),
    @XmlEnumValue("0483")katrineholm(4, 483, "Katrineholm"),
    @XmlEnumValue("0484")eskilstuna(4, 484, "Eskilstuna"),
    @XmlEnumValue("0486")strangnas(4, 486, "Strängnäs"),
    @XmlEnumValue("0488")trosa(4, 488, "Trosa"),

    // Östergötland municipalities
    @XmlEnumValue("0509")odeshog(5, 509, "Ödeshög"),
    @XmlEnumValue("0512")ydre(5, 512, "Ydre"),
    @XmlEnumValue("0513")kinda(5, 513, "Kinda"),
    @XmlEnumValue("0560")boxholm(5, 560, "Boxholm"),
    @XmlEnumValue("0561")atvidaberg(5, 561, "Åtvidaberg"),
    @XmlEnumValue("0562")finspang(5, 562, "Finspång"),
    @XmlEnumValue("0563")valdemarsvik(5, 563, "Valdemarsvik"),
    @XmlEnumValue("0580")linkoping(5, 580, "Linköping"),
    @XmlEnumValue("0581")norrkoping(5, 581, "Norrköping"),
    @XmlEnumValue("0582")soderkoping(5, 582, "Söderköping"),
    @XmlEnumValue("0583")motala(5, 583, "Motala"),
    @XmlEnumValue("0584")vadstena(5, 584, "Vadstena"),
    @XmlEnumValue("0586")mjolby(5, 586, "Mjölby"),

    // Jönköping municipalities
    @XmlEnumValue("0604")aneby(6, 604, "Aneby"),
    @XmlEnumValue("0617")gnosjo(6, 617, "Gnosjö"),
    @XmlEnumValue("0642")mullsjo(6, 642, "Mullsjö"),
    @XmlEnumValue("0643")habo_jonkoping(6, 643, "Habo"),
    @XmlEnumValue("0662")gislaved(6, 662, "Gislaved"),
    @XmlEnumValue("0665")vaggeryd(6, 665, "Vaggeryd"),
    @XmlEnumValue("0680")jonkoping(6, 680, "Jönköping"),
    @XmlEnumValue("0682")nassjo(6, 682, "Nässjö"),
    @XmlEnumValue("0683")varnamo(6, 683, "Värnamo"),
    @XmlEnumValue("0684")savsjo(6, 684, "Sävsjö"),
    @XmlEnumValue("0685")vetlanda(6, 685, "Vetlanda"),
    @XmlEnumValue("0686")eksjo(6, 686, "Eksjö"),
    @XmlEnumValue("0687")tranas(6, 687, "Tranås"),

    // Kronoberg municipalities
    @XmlEnumValue("0760")uppvidinge(7, 760, "Uppvidinge"),
    @XmlEnumValue("0761")lessebo(7, 761, "Lessebo"),
    @XmlEnumValue("0763")tingsryd(7, 763, "Tingsryd"),
    @XmlEnumValue("0764")alvesta(7, 764, "Alvesta"),
    @XmlEnumValue("0765")almhult(7, 765, "Älmhult"),
    @XmlEnumValue("0767")markaryd(7, 767, "Markaryd"),
    @XmlEnumValue("0780")vaxjo(7, 780, "Växjö"),
    @XmlEnumValue("0781")ljungby(7, 781, "Ljungby"),

    // Kalmar municipalities
    @XmlEnumValue("0821")hogsby(8, 821, "Högsby"),
    @XmlEnumValue("0834")torsas(8, 834, "Torsås"),
    @XmlEnumValue("0830")morbylanga(8, 840, "Mörbylånga"),
    @XmlEnumValue("0860")hultsfred(8, 860, "Hultsfred"),
    @XmlEnumValue("0861")monsteras(8, 861, "Mönsterås"),
    @XmlEnumValue("0862")emmaboda(8, 862, "Emmaboda"),
    @XmlEnumValue("0880")kalmar(8, 880, "Kalmar"),
    @XmlEnumValue("0881")nybro(8, 881, "Nybro"),
    @XmlEnumValue("0882")oskarshamn(8, 882, "Oskarshamn"),
    @XmlEnumValue("0883")vastervik(8, 883, "Västervik"),
    @XmlEnumValue("0884")vimmerby(8, 884, "Vimmerby"),
    @XmlEnumValue("0885")borgholm(8, 885, "Borgholm"),

    // Gotland municipality
    @XmlEnumValue("0980")gotland(9, 980, "Gotland"),

    // Blekinge municipalities
    @XmlEnumValue("1060")olofstrom(10, 1060, "Olofström"),
    @XmlEnumValue("1080")karlskrona(10, 1080, "Karlskrona"),
    @XmlEnumValue("1081")ronneby(10, 1081, "Ronneby"),
    @XmlEnumValue("1082")karlshamn(10, 1082, "Karlshamn"),
    @XmlEnumValue("1083")solvesborg(10, 1083, "Sölvesborg"),

    // Skåne municipalities
    @XmlEnumValue("1214")svalov(12, 1214, "Svalöv"),
    @XmlEnumValue("1230")staffanstorp(12, 1230, "Staffanstorp"),
    @XmlEnumValue("1231")bjurlov(12, 1231, "Burlöv"),
    @XmlEnumValue("1233")vellinge(12, 1233, "Vellinge"),
    @XmlEnumValue("1256")ostra_goinge(12, 1256, "Östra Göinge"),
    @XmlEnumValue("1257")orkelljunga(12, 1257, "Örkelljunga"),
    @XmlEnumValue("1260")bjuv(12, 1260, "Bjuv"),
    @XmlEnumValue("1261")kavlinge(12, 1261, "Kävlinge"),
    @XmlEnumValue("1262")lomma(12, 1262, "Lomma"),
    @XmlEnumValue("1263")svedala(12, 1263, "Svedala"),
    @XmlEnumValue("1264")skurup(12, 1264, "Skurup"),
    @XmlEnumValue("1265")sjobo(12, 1265, "Sjöbo"),
    @XmlEnumValue("1266")horby(12, 1266, "Hörby"),
    @XmlEnumValue("1267")hoor(12, 1267, "Höör"),
    @XmlEnumValue("1270")tomelilla(12, 1270, "Tomelilla"),
    @XmlEnumValue("1272")bromolla(12, 1272, "Bromölla"),
    @XmlEnumValue("1273")osby(12, 1273, "Osby"),
    @XmlEnumValue("1275")perstorp(12, 1275, "Perstorp"),
    @XmlEnumValue("1276")klippan(12, 1276, "Klippan"),
    @XmlEnumValue("1277")astorp(12, 1277, "Åstorp"),
    @XmlEnumValue("1278")bastad(12, 1278, "Båstad"),
    @XmlEnumValue("1280")malmo(12, 1280, "Malmö"),
    @XmlEnumValue("1281")lund(12, 1281, "Lund"),
    @XmlEnumValue("1282")landskrona(12, 1282, "Landskrona"),
    @XmlEnumValue("1283")helsingborg(12, 1283, "Helsingborg"),
    @XmlEnumValue("1284")hoganas(12, 1284, "Höganäs"),
    @XmlEnumValue("1285")eslov(12, 1285, "Eslöv"),
    @XmlEnumValue("1286")ystad(12, 1286, "Ystad"),
    @XmlEnumValue("1287")trelleborg(12, 1287, "Trelleborg"),
    @XmlEnumValue("1290")kristianstad(12, 1290, "Kristianstad"),
    @XmlEnumValue("1291")simrishamn(12, 1291, "Simrishamn"),
    @XmlEnumValue("1292")angelholm(12, 1292, "Ängelholm"),
    @XmlEnumValue("1293")hassleholm(12, 1293, "Hässleholm"),

    // Halland municipalities
    @XmlEnumValue("1315")hylte(13, 1315, "Hylte"),
    @XmlEnumValue("1380")halmstad(13, 1380, "Halmstad"),
    @XmlEnumValue("1381")laholm(13, 1381, "Laholm"),
    @XmlEnumValue("1382")falkenberg(13, 1382, "Falkenberg"),
    @XmlEnumValue("1383")varberg(13, 1383, "Varberg"),
    @XmlEnumValue("1384")kungsbacka(13, 1384, "Kungsbacka"),

    // Västra Götaland municipalities
    @XmlEnumValue("1401")harryda(14, 1401, "Härryda"),
    @XmlEnumValue("1402")partille(14, 1402, "Partille"),
    @XmlEnumValue("1407")ockero(14, 1407, "Öckerö"),
    @XmlEnumValue("1415")stenungsund(14, 1415, "Stenungsund"),
    @XmlEnumValue("1419")tjorn(14, 1419, "Tjörn"),
    @XmlEnumValue("1421")orust(14, 1421, "Orust"),
    @XmlEnumValue("1427")sotenas(14, 1427, "Sotenäs"),
    @XmlEnumValue("1430")munkedal(14, 1430, "Munkedal"),
    @XmlEnumValue("1435")tanum(14, 1435, "Tanum"),
    @XmlEnumValue("1438")dals_ed(14, 1438, "Dals-Ed"),
    @XmlEnumValue("1439")fargelanda(14, 1439, "Färgelanda"),
    @XmlEnumValue("1440")ale(14, 1440, "Ale"),
    @XmlEnumValue("1441")lerum(14, 1441, "Lerum"),
    @XmlEnumValue("1442")vargarda(14, 1442, "Vårgårda"),
    @XmlEnumValue("1443")bollebygd(14, 1443, "Bollebygd"),
    @XmlEnumValue("1444")grastorp(14, 1444, "Grästorp"),
    @XmlEnumValue("1445")essunga(14, 1445, "Essunga"),
    @XmlEnumValue("1446")karlsborg(14, 1446, "Karlsborg"),
    @XmlEnumValue("1447")gullspang(14, 1447, "Gullspång"),
    @XmlEnumValue("1452")tranemo(14, 1452, "Tranemo"),
    @XmlEnumValue("1460")bengtsfors(14, 1460, "Bengtsfors"),
    @XmlEnumValue("1461")mellerud(14, 1461, "Mellerud"),
    @XmlEnumValue("1462")lilla_edet(14, 1462, "Lilla Edet"),
    @XmlEnumValue("1463")mark(14, 1463, "Mark"),
    @XmlEnumValue("1465")svenljunga(14, 1465, "Svenljunga"),
    @XmlEnumValue("1466")herrljunga(14, 1466, "Herrljunga"),
    @XmlEnumValue("1470")vara(14, 1470, "Vara"),
    @XmlEnumValue("1471")gotene(14, 1471, "Götene"),
    @XmlEnumValue("1472")tibro(14, 1472, "Tibro"),
    @XmlEnumValue("1473")toreboda(14, 1473, "Töreboda"),
    @XmlEnumValue("1480")goteborg(14, 1480, "Göteborg"),
    @XmlEnumValue("1481")molndal(14, 1481, "Mölndal"),
    @XmlEnumValue("1482")kungalv(14, 1482, "Kungälv"),
    @XmlEnumValue("1484")lysekil(14, 1484, "Lysekil"),
    @XmlEnumValue("1485")uddevalla(14, 1485, "Uddevalla"),
    @XmlEnumValue("1486")stromstad(14, 1486, "Strömstad"),
    @XmlEnumValue("1487")vanersborg(14, 1487, "Vänersborg"),
    @XmlEnumValue("1488")trollhattan(14, 1488, "Trollhättan"),
    @XmlEnumValue("1489")alingsas(14, 1489, "Alingsås"),
    @XmlEnumValue("1490")boras(14, 1490, "Borås"),
    @XmlEnumValue("1491")ulricehamn(14, 1491, "Ulricehamn"),
    @XmlEnumValue("1492")amal(14, 1492, "Åmål"),
    @XmlEnumValue("1493")mariestad(14, 1493, "Mariestad"),
    @XmlEnumValue("1494")lidkoping(14, 1494, "Lidköping"),
    @XmlEnumValue("1495")skara(14, 1495, "Skara"),
    @XmlEnumValue("1496")skovde(14, 1496, "Skövde"),
    @XmlEnumValue("1497")hjo(14, 1497, "Hjo"),
    @XmlEnumValue("1498")tidaholm(14, 1498, "Tidaholm"),
    @XmlEnumValue("1499")falkoping(14, 1499, "Falköping"),

    // Värmland municipalities
    @XmlEnumValue("1715")kil(17, 1715, "Kil"),
    @XmlEnumValue("1730")eda(17, 1730, "Eda"),
    @XmlEnumValue("1737")torsby(17, 1737, "Torsby"),
    @XmlEnumValue("1760")storfors(17, 1760, "Storfors"),
    @XmlEnumValue("1761")hammaro(17, 1761, "Hammarö"),
    @XmlEnumValue("1762")munkfors(17, 1762, "Munkfors"),
    @XmlEnumValue("1763")forshaga(17, 1763, "Forshaga"),
    @XmlEnumValue("1764")grums(17, 1764, "Grums"),
    @XmlEnumValue("1765")arjang(17, 1765, "Årjäng"),
    @XmlEnumValue("1766")sunne(17, 1766, "Sunne"),
    @XmlEnumValue("1780")karlstad(17, 1780, "Karlstad"),
    @XmlEnumValue("1781")kristinehamn(17, 1781, "Kristinehamn"),
    @XmlEnumValue("1782")filipstad(17, 1782, "Filipstad"),
    @XmlEnumValue("1783")hagfors(17, 1783, "Hagfors"),
    @XmlEnumValue("1784")arvika(17, 1784, "Arvika"),
    @XmlEnumValue("1785")saffle(17, 1785, "Säffle"),

    // Örebro municipalities
    @XmlEnumValue("1814")lekeberg(18, 1814, "Lekeberg"),
    @XmlEnumValue("1860")laxa(18, 1860, "Laxå"),
    @XmlEnumValue("1861")hallsberg(18, 1861, "Hallsberg"),
    @XmlEnumValue("1862")degerfors(18, 1862, "Degerfors"),
    @XmlEnumValue("1863")hallefors(18, 1863, "Hällefors"),
    @XmlEnumValue("1864")ljusnarsberg(18, 1864, "Ljusnarsberg"),
    @XmlEnumValue("1880")orebro(18, 1880, "Örebro"),
    @XmlEnumValue("1881")kumla(18, 1881, "Kumla"),
    @XmlEnumValue("1882")askersund(18, 1882, "Askersund"),
    @XmlEnumValue("1883")karlskoga(18, 1883, "Karlskoga"),
    @XmlEnumValue("1884")nora(18, 1884, "Nora"),
    @XmlEnumValue("1885")lindesberg(18, 1885, "Lindesberg"),

    // Västmanland municipalities
    @XmlEnumValue("1904")skinskatteberg(19, 1904, "Skinnskatteberg"),
    @XmlEnumValue("1907")surahammar(19, 1907, "Surahammar"),
    @XmlEnumValue("1960")kungsor(19, 1960, "Kungsör"),
    @XmlEnumValue("1961")hallstahammar(19, 1961, "Hallstahammar"),
    @XmlEnumValue("1962")norberg(19, 1962, "Norberg"),
    @XmlEnumValue("1980")vasteras(19, 1980, "Västerås"),
    @XmlEnumValue("1981")sala(19, 1981, "Sala"),
    @XmlEnumValue("1982")fagersta(19, 1982, "Fagersta"),
    @XmlEnumValue("1983")koping(19, 1983, "Köping"),
    @XmlEnumValue("1984")arboga(19, 1984, "Arboga"),

    // Dalarna municipalities
    @XmlEnumValue("2021")vansbro(20, 2021, "Vansbro"),
    @XmlEnumValue("2023")malung_salen(20, 2023, "Malung-Sälen"),
    @XmlEnumValue("2026")gagnef(20, 2026, "Gagnef"),
    @XmlEnumValue("2029")leksand(20, 2029, "Leksand"),
    @XmlEnumValue("2031")rattvik(20, 2031, "Rättvik"),
    @XmlEnumValue("2034")orsa(20, 2034, "Orsa"),
    @XmlEnumValue("2039")alvdalen(20, 2039, "Älvdalen"),
    @XmlEnumValue("2061")smedjebacken(20, 2061, "Smedjebacken"),
    @XmlEnumValue("2062")mora(20, 2062, "Mora"),
    @XmlEnumValue("2080")falun(20, 2080, "Falun"),
    @XmlEnumValue("2081")borlange(20, 2081, "Borlänge"),
    @XmlEnumValue("2082")sater(20, 2082, "Säter"),
    @XmlEnumValue("2083")hedemora(20, 2083, "Hedemora"),
    @XmlEnumValue("2084")avesta(20, 2084, "Avesta"),
    @XmlEnumValue("2085")ludvika(20, 2085, "Ludvika"),

    // Gävleborg municipalities
    @XmlEnumValue("2101")ockelbo(21, 2101, "Ockelbo"),
    @XmlEnumValue("2104")hofors(21, 2104, "Hofors"),
    @XmlEnumValue("2121")ovanaker(21, 2121, "Ovanåker"),
    @XmlEnumValue("2132")nordanstig(21, 2132, "Nordanstig"),
    @XmlEnumValue("2161")ljusdal(21, 2161, "Ljusdal"),
    @XmlEnumValue("2180")gavle(21, 2180, "Gävle"),
    @XmlEnumValue("2181")sandviken(21, 2181, "Sandviken"),
    @XmlEnumValue("2182")soderhamn(21, 2182, "Söderhamn"),
    @XmlEnumValue("2183")bollnas(21, 2183, "Bollnäs"),
    @XmlEnumValue("2184")hudiksvall(21, 2184, "Hudiksvall"),

    // Västernorrland municipalities
    @XmlEnumValue("2260")ange(22, 2260, "Ånge"),
    @XmlEnumValue("2262")timra(22, 2262, "Timrå"),
    @XmlEnumValue("2280")harnosand(22, 2280, "Härnösand"),
    @XmlEnumValue("2280")sundsvall(22, 2281, "Sundsvall"),
    @XmlEnumValue("2282")kramfors(22, 2282, "Kramfors"),
    @XmlEnumValue("2283")solleftea(22, 2283, "Sollefteå"),
    @XmlEnumValue("2283")ornskoldsvik(22, 2284, "Örnsköldsvik"),

    // Jämtland municipalities
    @XmlEnumValue("2303")ragunda(23, 2303, "Ragunda"),
    @XmlEnumValue("2305")bracke(23, 2305, "Bräcke"),
    @XmlEnumValue("2319")krokom(23, 2309, "Krokom"),
    @XmlEnumValue("2313")stromsund(23, 2313, "Strömsund"),
    @XmlEnumValue("2321")are(23, 2321, "Åre"),
    @XmlEnumValue("2326")berg(23, 2326, "Berg"),
    @XmlEnumValue("2361")harjedalen(23, 2361, "Härjedalen"),
    @XmlEnumValue("2380")ostersund(23, 2380, "Östersund"),

    // Västerbotten municipalities
    @XmlEnumValue("2401")nordmaling(24, 2401, "Nordmaling"),
    @XmlEnumValue("2403")bjurholm(24, 2403, "Bjurholm"),
    @XmlEnumValue("2404")vindeln(24, 2404, "Vindeln"),
    @XmlEnumValue("2409")robertsfors(24, 2409, "Robertsfors"),
    @XmlEnumValue("2417")nordsjo(24, 2417, "Norsjö"),
    @XmlEnumValue("2418")mala(24, 2418, "Malå"),
    @XmlEnumValue("2421")storuman(24, 2421, "Storuman"),
    @XmlEnumValue("2422")sorsele(24, 2422, "Sorsele"),
    @XmlEnumValue("2425")dorotea(24, 2425, "Dorotea"),
    @XmlEnumValue("2460")vannas(24, 2460, "Vännäs"),
    @XmlEnumValue("2462")vilhelmina(24, 2462, "Vilhelmina"),
    @XmlEnumValue("2463")asele(24, 2463, "Åsele"),
    @XmlEnumValue("2480")umea(24, 2480, "Umeå"),
    @XmlEnumValue("2481")lycksele(24, 2481, "Lycksele"),
    @XmlEnumValue("2482")skelleftea(24, 2482, "Skellefteå"),

    // Norrbotten municipalities
    @XmlEnumValue("2505")arvidsjaur(25, 2505, "Arvidsjaur"),
    @XmlEnumValue("2506")arjeplog(25, 2506, "Arjeplog"),
    @XmlEnumValue("2510")jokkmokk(25, 2510, "Jokkmokk"),
    @XmlEnumValue("2513")overkalix(25, 2513, "Överkalix"),
    @XmlEnumValue("2514")kalix(25, 2514, "Kalix"),
    @XmlEnumValue("2518")overtornea(25, 2518, "Övertorneå"),
    @XmlEnumValue("2521")pajala(25, 2521, "Pajala"),
    @XmlEnumValue("2523")gallivare(25, 2523, "Gällivare"),
    @XmlEnumValue("2560")alvsbyn(25, 2560, "Älvsbyn"),
    @XmlEnumValue("2580")lulea(25, 2580, "Luleå"),
    @XmlEnumValue("2581")pitea(25, 2581, "Piteå"),
    @XmlEnumValue("2582")boden(25, 2582, "Boden"),
    @XmlEnumValue("2583")haparanda(25, 2583, "Haparanda"),
    @XmlEnumValue("2584")kiruna(25, 2584, "Kiruna");

    // Internal state
    private static final DecimalFormat FOUR_DIGIT_FORMAT = new DecimalFormat("####");
    private int municipalityId;
    private County county;
    private String municipalityName;

    Municipality(final int countyId,
            final int municipalityId,
            final String municipalityName) {

        // Assign internal state
        this.municipalityId = municipalityId;
        this.municipalityName = municipalityName;

        // Synthesize derived state
        this.county = County.getCountyById(countyId);
    }

    /**
     * @return The county of this Municipality.
     */
    public County getCounty() {
        return county;
    }

    /**
     * @return The name of this Municipality.
     */
    public String getMunicipalityName() {
        return municipalityName;
    }

    /**
     * @return The Municipality ID of this Municipality.
     */
    public int getMunicipalityId() {
        return municipalityId;
    }

    /**
     * @return The 4-digit municipality ID as a String.
     */
    public String get4DigitMunicipalityId() {
        return FOUR_DIGIT_FORMAT.format(municipalityId);
    }

    /**
     * Acquires all municipalities within the supplied County.
     *
     * @param county The non-null county for which all Municipalities should be retrieved.
     * @return A SortedSet containing the Municipalities within the supplied County.
     */
    public static SortedSet<Municipality> getMunicipalitiesIn(final County county) {

        // Check sanity
        Validate.notNull(county, "Cannot handle null 'county' argument.");

        // Find all Municipalities within the supplied county.
        final SortedSet<Municipality> toReturn = new TreeSet<Municipality>();
        for (Municipality current : values()) {
            if (current.getCounty() == county) {
                toReturn.add(current);
            }
        }

        // All done.
        return toReturn;
    }
}
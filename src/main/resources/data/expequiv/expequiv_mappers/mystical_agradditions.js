//#define ${mod} mysticalagradditions
//#require isModLoaded("${mod}");

function registerEMC(configs) {
    configs.addEMC(getItem("${mod}", "withering_soul"), "WitheringSoul", 2048);
    configs.addEMC(getItem("${mod}", "dragon_scale"), "DragonScale", 6144);
}
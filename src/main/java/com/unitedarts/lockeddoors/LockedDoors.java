package com.unitedarts.lockeddoors;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LockedDoors.MODID)
public class LockedDoors {
	// Define mod id in a common place for everything to reference
	public static final String MODID = "lockeddoors";
	// Directly reference a slf4j logger
	private static final Logger LOGGER = LogUtils.getLogger();
	// Create a Deferred Register to hold Blocks
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	// Create a Deferred Register to hold Items
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	// Create a Deferred Register to hold Sounds
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
			MODID);
	// Create a Deferred Register to hold CreativeModeTabs
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, MODID);

	// Sounds
	public static final RegistryObject<SoundEvent> LOCKED_DOOR_SOUND = SOUNDS.register("lockeddoor",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "lockeddoor")));
	public static final RegistryObject<SoundEvent> UNLOCK_DOOR_SOUND = SOUNDS.register("unlockdoor",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "unlockdoor")));
	public static final RegistryObject<SoundEvent> LOCK_DOOR_SOUND = SOUNDS.register("lockdoor",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "lockdoor")));

	// Yellow Door
	public static final RegistryObject<Block> YELLOW_DOOR_BLOCK = BLOCKS.register("yellow_door",
			() -> new LockableDoor(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));
	public static final RegistryObject<Item> YELLOW_DOOR_BLOCK_ITEM = ITEMS.register("yellow_door",
			() -> new BlockItem(YELLOW_DOOR_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<Item> YELLOW_DOOR_KEY_ITEM = ITEMS.register("yellow_door_key",
			() -> new Item(new Item.Properties()));

	// Creative tab
	public static final RegistryObject<CreativeModeTab> LOCKEDDOORS_TAB = CREATIVE_MODE_TABS.register("lockeddoors_tab",
			() -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT)
					.icon(() -> YELLOW_DOOR_BLOCK_ITEM.get().getDefaultInstance())
					.displayItems((parameters, output) -> {
						output.accept(YELLOW_DOOR_BLOCK_ITEM.get());
					}).build());

	public LockedDoors() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);
		// Register the Deferred Register to the mod event bus so sounds get registered
		SOUNDS.register(modEventBus);
		// Register the Deferred Register to the mod event bus so blocks get registered
		BLOCKS.register(modEventBus);
		// Register the Deferred Register to the mod event bus so items get registered
		ITEMS.register(modEventBus);
		// Register the Deferred Register to the mod event bus so tabs get registered
		CREATIVE_MODE_TABS.register(modEventBus);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		// Register the item to a creative tab
		modEventBus.addListener(this::addCreative);

		// Register our mod's ForgeConfigSpec so that Forge can create and load the
		// config file
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		// Some common setup code
		LOGGER.info("LockedDoors SETUP");

		LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

		Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
	}

	// Add the example block item to the building blocks tab
	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == LOCKEDDOORS_TAB.getKey()) {
			event.accept(YELLOW_DOOR_BLOCK_ITEM);
			event.accept(YELLOW_DOOR_KEY_ITEM);
		}
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		// Do something when the server starts
		LOGGER.info("Server starting");
	}

	// You can use EventBusSubscriber to automatically register all static methods
	// in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			// Some client setup code
			LOGGER.info("Client SETUP");
			LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
		}
	}
}

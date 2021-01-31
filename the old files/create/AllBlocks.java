package com.simibubi.kinetic_api;

import static com.simibubi.kinetic_api.AllMovementBehaviours.addMovementBehaviour;
import static com.simibubi.kinetic_api.AllTags.tagBlockAndItem;
import static com.simibubi.kinetic_api.content.AllSections.SCHEMATICS;
import static com.simibubi.kinetic_api.foundation.data.BlockStateGen.axisBlock;
import static com.simibubi.kinetic_api.foundation.data.BlockStateGen.oxidizedBlockstate;
import static com.simibubi.kinetic_api.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.kinetic_api.foundation.data.ModelGen.customItemModel;
import static com.simibubi.kinetic_api.foundation.data.ModelGen.oxidizedItemModel;

import com.simibubi.kinetic_api.AllTags.AllBlockTags;
import com.simibubi.kinetic_api.AllTags.AllItemTags;
import com.simibubi.kinetic_api.content.AllSections;
import com.simibubi.kinetic_api.content.contraptions.base.CasingBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.DrillBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.DrillMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.actors.HarvesterBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.HarvesterMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.actors.PloughBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.PloughMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.actors.PortableStorageInterfaceBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.PortableStorageInterfaceMovement;
import com.simibubi.kinetic_api.content.contraptions.components.actors.SawMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.actors.SeatBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.SeatMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.clock.CuckooClockBlock;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.CrafterCTBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.kinetic_api.content.contraptions.components.crank.HandCrankBlock;
import com.simibubi.kinetic_api.content.contraptions.components.crank.ValveHandleBlock;
import com.simibubi.kinetic_api.content.contraptions.components.crusher.CrushingWheelBlock;
import com.simibubi.kinetic_api.content.contraptions.components.crusher.CrushingWheelControllerBlock;
import com.simibubi.kinetic_api.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.kinetic_api.content.contraptions.components.deployer.DeployerMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.kinetic_api.content.contraptions.components.fan.NozzleBlock;
import com.simibubi.kinetic_api.content.contraptions.components.flywheel.FlywheelBlock;
import com.simibubi.kinetic_api.content.contraptions.components.flywheel.FlywheelGenerator;
import com.simibubi.kinetic_api.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.kinetic_api.content.contraptions.components.millstone.MillstoneBlock;
import com.simibubi.kinetic_api.content.contraptions.components.mixer.BasinOperatorBlockItem;
import com.simibubi.kinetic_api.content.contraptions.components.mixer.MechanicalMixerBlock;
import com.simibubi.kinetic_api.content.contraptions.components.motor.CreativeMotorBlock;
import com.simibubi.kinetic_api.content.contraptions.components.motor.CreativeMotorGenerator;
import com.simibubi.kinetic_api.content.contraptions.components.press.MechanicalPressBlock;
import com.simibubi.kinetic_api.content.contraptions.components.saw.SawBlock;
import com.simibubi.kinetic_api.content.contraptions.components.saw.SawGenerator;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.ClockworkBearingBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.StabilizedBearingMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis.LinearChassisBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis.LinearChassisBlock.ChassisCTBehaviour;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis.RadialChassisBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockItem;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.kinetic_api.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.kinetic_api.content.contraptions.components.tracks.ControllerRailGenerator;
import com.simibubi.kinetic_api.content.contraptions.components.tracks.ReinforcedRailBlock;
import com.simibubi.kinetic_api.content.contraptions.components.turntable.TurntableBlock;
import com.simibubi.kinetic_api.content.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.PipeAttachmentModel;
import com.simibubi.kinetic_api.content.contraptions.fluids.PumpBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.actors.HosePulleyBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.actors.ItemDrainBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.actors.SpoutBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.BracketBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.BracketBlockItem;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.BracketGenerator;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.FluidValveBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.SmartFluidPipeBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.pipes.SmartFluidPipeGenerator;
import com.simibubi.kinetic_api.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.kinetic_api.content.contraptions.fluids.tank.FluidTankGenerator;
import com.simibubi.kinetic_api.content.contraptions.fluids.tank.FluidTankItem;
import com.simibubi.kinetic_api.content.contraptions.fluids.tank.FluidTankModel;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinBlock;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinGenerator;
import com.simibubi.kinetic_api.content.contraptions.processing.BasinMovementBehaviour;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerBlockItem;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.LitBlazeBurnerBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.advanced.sequencer.SequencedGearshiftBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.advanced.sequencer.SequencedGearshiftGenerator;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltColor;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltGenerator;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltModel;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.BracketedKineticBlockModel;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.AdjustablePulleyBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.ClutchBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.EncasedBeltGenerator;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.EncasedCTBehaviour;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.GearshiftBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.gauge.GaugeBlock;
import com.simibubi.kinetic_api.content.contraptions.relays.gauge.GaugeGenerator;
import com.simibubi.kinetic_api.content.contraptions.relays.gearbox.GearboxBlock;
import com.simibubi.kinetic_api.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.belts.tunnel.BrassTunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.belts.tunnel.BrassTunnelCTBehaviour;
import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteBlock;
import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteGenerator;
import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteItem;
import com.simibubi.kinetic_api.content.logistics.block.depot.DepotBlock;
import com.simibubi.kinetic_api.content.logistics.block.diodes.AbstractDiodeGenerator;
import com.simibubi.kinetic_api.content.logistics.block.diodes.AdjustableRepeaterBlock;
import com.simibubi.kinetic_api.content.logistics.block.diodes.AdjustableRepeaterGenerator;
import com.simibubi.kinetic_api.content.logistics.block.diodes.PoweredLatchBlock;
import com.simibubi.kinetic_api.content.logistics.block.diodes.PoweredLatchGenerator;
import com.simibubi.kinetic_api.content.logistics.block.diodes.PulseRepeaterBlock;
import com.simibubi.kinetic_api.content.logistics.block.diodes.PulseRepeaterGenerator;
import com.simibubi.kinetic_api.content.logistics.block.diodes.ToggleLatchBlock;
import com.simibubi.kinetic_api.content.logistics.block.diodes.ToggleLatchGenerator;
import com.simibubi.kinetic_api.content.logistics.block.funnel.AndesiteBeltFunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.AndesiteFunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BeltFunnelGenerator;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BrassBeltFunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.BrassFunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelMovementBehaviour;
import com.simibubi.kinetic_api.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.kinetic_api.content.logistics.block.inventories.CreativeCrateBlock;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmBlock;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmItem;
import com.simibubi.kinetic_api.content.logistics.block.redstone.AnalogLeverBlock;
import com.simibubi.kinetic_api.content.logistics.block.redstone.ContactMovementBehaviour;
import com.simibubi.kinetic_api.content.logistics.block.redstone.ContentObserverBlock;
import com.simibubi.kinetic_api.content.logistics.block.redstone.NixieTubeBlock;
import com.simibubi.kinetic_api.content.logistics.block.redstone.NixieTubeGenerator;
import com.simibubi.kinetic_api.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.kinetic_api.content.logistics.block.redstone.RedstoneLinkBlock;
import com.simibubi.kinetic_api.content.logistics.block.redstone.RedstoneLinkGenerator;
import com.simibubi.kinetic_api.content.logistics.block.redstone.StockpileSwitchBlock;
import com.simibubi.kinetic_api.content.schematics.block.SchematicTableBlock;
import com.simibubi.kinetic_api.content.schematics.block.SchematicannonBlock;
import com.simibubi.kinetic_api.foundation.config.StressConfigDefaults;
import com.simibubi.kinetic_api.foundation.data.AssetLookup;
import com.simibubi.kinetic_api.foundation.data.BlockStateGen;
import com.simibubi.kinetic_api.foundation.data.BuilderTransformers;
import com.simibubi.kinetic_api.foundation.data.CreateRegistrate;
import com.simibubi.kinetic_api.foundation.data.ModelGen;
import com.simibubi.kinetic_api.foundation.data.SharedProperties;
import com.simibubi.kinetic_api.foundation.item.TooltipHelper;
import com.simibubi.kinetic_api.foundation.utility.DyeHelper;
import com.simibubi.kinetic_api.foundation.worldgen.OxidizingBlock;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.SnowyBlock;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DebugStickItem;
import net.minecraft.item.HoeItem;
import net.minecraft.stat.StatHandler;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

public class AllBlocks {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.baseCreativeTab);

	// Schematics

	static {
		REGISTRATE.startSection(SCHEMATICS);
	}

	public static final BlockEntry<SchematicannonBlock> SCHEMATICANNON =
		REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.initialProperties(() -> BellBlock.as)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SchematicTableBlock> SCHEMATIC_TABLE =
		REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.initialProperties(() -> BellBlock.lY)
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
				.getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();

	// Kinetics

	static {
		REGISTRATE.startSection(AllSections.KINETICS);
	}

	public static final BlockEntry<ShaftBlock> SHAFT = REGISTRATE.block("shaft", ShaftBlock::new)
		.initialProperties(SharedProperties::stone)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.simpleItem()
		.register();

	public static final BlockEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small)
		.initialProperties(SharedProperties::stone)
		.transform(StressConfigDefaults.setNoImpact())
		.properties(p -> p.a(SnowyBlock.SNOWY))
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
		.item(CogwheelBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<CogWheelBlock> LARGE_COGWHEEL =
		REGISTRATE.block("large_cogwheel", CogWheelBlock::large)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.a(SnowyBlock.SNOWY))
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
			.item(CogwheelBlockItem::new)
			.build()
			.register();

	public static final BlockEntry<EncasedShaftBlock> ANDESITE_ENCASED_SHAFT =
		REGISTRATE.block("andesite_encased_shaft", EncasedShaftBlock::andesite)
			.transform(BuilderTransformers.encasedShaft("andesite", AllSpriteShifts.ANDESITE_CASING))
			.register();

	public static final BlockEntry<EncasedShaftBlock> BRASS_ENCASED_SHAFT =
		REGISTRATE.block("brass_encased_shaft", EncasedShaftBlock::brass)
			.transform(BuilderTransformers.encasedShaft("brass", AllSpriteShifts.BRASS_CASING))
			.register();

	public static final BlockEntry<GearboxBlock> GEARBOX = REGISTRATE.block("gearbox", GearboxBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(PistonBlockEntity.c::b)
		.transform(StressConfigDefaults.setNoImpact())
		.onRegister(CreateRegistrate.connectedTextures(new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
		.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
			(s, f) -> f.getAxis() == s.c(GearboxBlock.AXIS))))
		.blockstate((c, p) -> axisBlock(c, p, $ -> AssetLookup.partialBaseModel(c, p), true))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ClutchBlock> CLUTCH = REGISTRATE.block("clutch", ClutchBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(PistonBlockEntity.c::b)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<GearshiftBlock> GEARSHIFT = REGISTRATE.block("gearshift", GearshiftBlock::new)
		.initialProperties(SharedProperties::stone)
		.properties(PistonBlockEntity.c::b)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<EncasedBeltBlock> ENCASED_CHAIN_DRIVE =
		REGISTRATE.block("encased_chain_drive", EncasedBeltBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(PistonBlockEntity.c::b)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> p.models()
				.getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<AdjustablePulleyBlock> ADJUSTABLE_CHAIN_GEARSHIFT =
		REGISTRATE.block("adjustable_chain_gearshift", AdjustablePulleyBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(PistonBlockEntity.c::b)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> {
				String powered = state.c(AdjustablePulleyBlock.POWERED) ? "_powered" : "";
				return p.models()
					.withExistingParent(c.getName() + "_" + suffix + powered, p.modLoc("block/encased_chain_drive/" + suffix))
					.texture("side", p.modLoc("block/" + c.getName() + powered));
			}).generate(c, p))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/encased_chain_drive/item"))
				.texture("side", p.modLoc("block/" + c.getName())))
			.build()
			.register();

	public static final BlockEntry<BeltBlock> BELT = REGISTRATE.block("belt", BeltBlock::new)
		.initialProperties(SharedProperties.beltMaterial, Fluids.w)
		.properties(p -> p.a(SnowyBlock.h))
		.properties(p -> p.d(0.8F))
		.blockstate(new BeltGenerator()::generate)
		.transform(StressConfigDefaults.setImpact(1.0))
		.onRegister(CreateRegistrate.blockColors(() -> BeltColor::new))
		.onRegister(CreateRegistrate.blockModel(() -> BeltModel::new))
		.register();

	public static final BlockEntry<CreativeMotorBlock> CREATIVE_MOTOR =
		REGISTRATE.block("creative_motor", CreativeMotorBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new CreativeMotorGenerator()::generate)
			.transform(StressConfigDefaults.setCapacity(16384.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<WaterWheelBlock> WATER_WHEEL = REGISTRATE.block("water_wheel", WaterWheelBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(PistonBlockEntity.c::b)
		.blockstate(BlockStateGen.horizontalWheelProvider(false))
		.addLayer(() -> VertexConsumerProvider::d)
		.transform(StressConfigDefaults.setCapacity(16.0))
		.simpleItem()
		.register();

	public static final BlockEntry<EncasedFanBlock> ENCASED_FAN = REGISTRATE.block("encased_fan", EncasedFanBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> VertexConsumerProvider::d)
		.transform(StressConfigDefaults.setCapacity(16.0))
		.transform(StressConfigDefaults.setImpact(2.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<NozzleBlock> NOZZLE = REGISTRATE.block("nozzle", NozzleBlock::new)
		.initialProperties(SharedProperties::stone)
		.tag(AllBlockTags.BRITTLE.tag)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.addLayer(() -> VertexConsumerProvider::d)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<TurntableBlock> TURNTABLE = REGISTRATE.block("turntable", TurntableBlock::new)
		.initialProperties(SharedProperties::wooden)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
		.transform(StressConfigDefaults.setImpact(4.0))
		.simpleItem()
		.register();

	public static final BlockEntry<HandCrankBlock> HAND_CRANK = REGISTRATE.block("hand_crank", HandCrankBlock::new)
		.initialProperties(SharedProperties::wooden)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(StressConfigDefaults.setCapacity(8.0))
		.tag(AllBlockTags.BRITTLE.tag)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<CuckooClockBlock> CUCKOO_CLOCK =
		REGISTRATE.block("cuckoo_clock", CuckooClockBlock::regular)
			.transform(BuilderTransformers.cuckooClock())
			.register();

	public static final BlockEntry<CuckooClockBlock> MYSTERIOUS_CUCKOO_CLOCK =
		REGISTRATE.block("mysterious_cuckoo_clock", CuckooClockBlock::mysterious)
			.transform(BuilderTransformers.cuckooClock())
			.lang("Cuckoo Clock")
			.onRegisterAfter(HoeItem.class, c -> TooltipHelper.referTo(c, CUCKOO_CLOCK))
			.register();

	public static final BlockEntry<MillstoneBlock> MILLSTONE = REGISTRATE.block("millstone", MillstoneBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.transform(StressConfigDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<CrushingWheelBlock> CRUSHING_WHEEL =
		REGISTRATE.block("crushing_wheel", CrushingWheelBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(PistonBlockEntity.c::b)
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.addLayer(() -> VertexConsumerProvider::d)
			.transform(StressConfigDefaults.setImpact(8.0))
			.simpleItem()
			.register();

	public static final BlockEntry<CrushingWheelControllerBlock> CRUSHING_WHEEL_CONTROLLER =
		REGISTRATE.block("crushing_wheel_controller", CrushingWheelControllerBlock::new)
			.initialProperties(() -> BellBlock.FACING)
			.blockstate((c, p) -> p.getVariantBuilder(c.get())
				.forAllStates(state -> ConfiguredModel.builder()
					.modelFile(p.models()
						.getExistingFile(p.mcLoc("block/air")))
					.build()))
			.register();

	public static final BlockEntry<MechanicalPressBlock> MECHANICAL_PRESS =
		REGISTRATE.block("mechanical_press", MechanicalPressBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(PistonBlockEntity.c::b)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(StressConfigDefaults.setImpact(8.0))
			.item(BasinOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<MechanicalMixerBlock> MECHANICAL_MIXER =
		REGISTRATE.block("mechanical_mixer", MechanicalMixerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(PistonBlockEntity.c::b)
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.addLayer(() -> VertexConsumerProvider::d)
			.transform(StressConfigDefaults.setImpact(4.0))
			.item(BasinOperatorBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<BasinBlock> BASIN = REGISTRATE.block("basin", BasinBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(new BasinGenerator()::generate)
		.onRegister(addMovementBehaviour(new BasinMovementBehaviour()))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<BlazeBurnerBlock> BLAZE_BURNER =
		REGISTRATE.block("blaze_burner", BlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.a($ -> 12))
			.addLayer(() -> VertexConsumerProvider::d)
			.tag(AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.FAN_HEATERS.tag)
			.loot((lt, block) -> lt.a(block, BlazeBurnerBlock.buildLootTable()))
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.item(BlazeBurnerBlockItem::withBlaze)
			.model(AssetLookup.<BlazeBurnerBlockItem>customItemModel("blaze_burner", "block_with_blaze"))
			.build()
			.register();

	public static final BlockEntry<LitBlazeBurnerBlock> LIT_BLAZE_BURNER =
		REGISTRATE.block("lit_blaze_burner", LitBlazeBurnerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(p -> p.a($ -> 12))
			.addLayer(() -> VertexConsumerProvider::d)
			.tag(AllBlockTags.FAN_TRANSPARENT.tag, AllBlockTags.FAN_HEATERS.tag)
			.loot((lt, block) -> lt.a(block, AllItems.EMPTY_BLAZE_BURNER.get()))
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), p.models()
				.getExistingFile(p.modLoc("block/blaze_burner/block_with_fire"))))
			.register();

	public static final BlockEntry<DepotBlock> DEPOT = REGISTRATE.block("depot", DepotBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
		.item()
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<ChuteBlock> CHUTE = REGISTRATE.block("chute", ChuteBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.addLayer(() -> VertexConsumerProvider::d)
		.blockstate(new ChuteGenerator()::generate)
		.item(ChuteItem::new)
		.transform(customItemModel("_", "block"))
		.register();

	public static final BlockEntry<GaugeBlock> SPEEDOMETER = REGISTRATE.block("speedometer", GaugeBlock::speed)
		.initialProperties(SharedProperties::wooden)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<GaugeBlock> STRESSOMETER = REGISTRATE.block("stressometer", GaugeBlock::stress)
		.initialProperties(SharedProperties::wooden)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(new GaugeGenerator()::generate)
		.item()
		.transform(ModelGen.customItemModel("gauge", "_", "item"))
		.register();

	public static final BlockEntry<BracketBlock> WOODEN_BRACKET = REGISTRATE.block("wooden_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("wooden")::generate)
		.item(BracketBlockItem::new)
		.transform(BracketGenerator.itemModel("wooden"))
		.register();

	public static final BlockEntry<BracketBlock> METAL_BRACKET = REGISTRATE.block("metal_bracket", BracketBlock::new)
		.blockstate(new BracketGenerator("metal")::generate)
		.item(BracketBlockItem::new)
		.transform(BracketGenerator.itemModel("metal"))
		.register();

	// Fluids

	public static final BlockEntry<FluidPipeBlock> FLUID_PIPE = REGISTRATE.block("fluid_pipe", FluidPipeBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate(BlockStateGen.pipe())
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<EncasedPipeBlock> ENCASED_FLUID_PIPE =
		REGISTRATE.block("encased_fluid_pipe", EncasedPipeBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(BeetrootsBlock.Properties::b)
			.blockstate(BlockStateGen.encasedPipe())
			.onRegister(CreateRegistrate.connectedTextures(new EncasedCTBehaviour(AllSpriteShifts.COPPER_CASING)))
			.onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.COPPER_CASING,
				(s, f) -> !s.c(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(f)))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.a(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<GlassFluidPipeBlock> GLASS_FLUID_PIPE =
		REGISTRATE.block("glass_fluid_pipe", GlassFluidPipeBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.addLayer(() -> VertexConsumerProvider::d)
			.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, s -> p.models()
				.getExistingFile(p.modLoc("block/fluid_pipe/window" + (s.c(GlassFluidPipeBlock.ALT) ? "_alt" : "")))))
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.loot((p, b) -> p.a(b, FLUID_PIPE.get()))
			.register();

	public static final BlockEntry<PumpBlock> MECHANICAL_PUMP = REGISTRATE.block("mechanical_pump", PumpBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(true))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.transform(StressConfigDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SmartFluidPipeBlock> SMART_FLUID_PIPE =
		REGISTRATE.block("smart_fluid_pipe", SmartFluidPipeBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.blockstate(new SmartFluidPipeGenerator()::generate)
			.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FluidValveBlock> FLUID_VALVE = REGISTRATE.block("fluid_valve", FluidValveBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((c, p) -> BlockStateGen.directionalAxisBlock(c, p,
			(state, vertical) -> AssetLookup.partialBaseModel(c, p, vertical ? "vertical" : "horizontal",
				state.c(FluidValveBlock.ENABLED) ? "open" : "closed")))
		.onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::new))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ValveHandleBlock> COPPER_VALVE_HANDLE =
		REGISTRATE.block("copper_valve_handle", ValveHandleBlock::copper)
			.transform(BuilderTransformers.valveHandle(null))
			.register();

	public static final BlockEntry<ValveHandleBlock>[] DYED_VALVE_HANDLES = new BlockEntry[DebugStickItem.values().length];

	static {
		for (DebugStickItem colour : DebugStickItem.values()) {
			String colourName = colour.a();
			DYED_VALVE_HANDLES[colour.ordinal()] =
				REGISTRATE.block(colourName + "_valve_handle", ValveHandleBlock::dyed)
					.transform(BuilderTransformers.valveHandle(colour))
					.recipe((c, p) -> ShapedRecipeJsonFactory.a(c.get())
						.pattern("#")
						.pattern("-")
						.a('#', DyeHelper.getTagOfDye(colour))
						.a('-', AllItemTags.VALVE_HANDLES.tag)
						.criterion("has_valve", RegistrateRecipeProvider.a(AllItemTags.VALVE_HANDLES.tag))
						.offerTo(p, Create.asResource("crafting/kinetics/" + c.getName() + "_from_other_valve_handle")))
					.register();
		}
	}

	public static final BlockEntry<FluidTankBlock> FLUID_TANK = REGISTRATE.block("fluid_tank", FluidTankBlock::regular)
		.initialProperties(SharedProperties::softMetal)
		.properties(PistonBlockEntity.c::b)
		.blockstate(new FluidTankGenerator()::generate)
		.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::standard))
		.addLayer(() -> VertexConsumerProvider::d)
		.item(FluidTankItem::new)
		.model(AssetLookup.<FluidTankItem>customItemModel("_", "block_single_window"))
		.build()
		.register();

	public static final BlockEntry<FluidTankBlock> CREATIVE_FLUID_TANK =
		REGISTRATE.block("creative_fluid_tank", FluidTankBlock::creative)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.properties(BeetrootsBlock.Properties::b)
			.blockstate(new FluidTankGenerator("creative_")::generate)
			.onRegister(CreateRegistrate.blockModel(() -> FluidTankModel::creative))
			.addLayer(() -> VertexConsumerProvider::d)
			.item(FluidTankItem::new)
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/fluid_tank/block_single_window"))
				.texture("5", p.modLoc("block/creative_fluid_tank_window_single"))
				.texture("1", p.modLoc("block/creative_fluid_tank"))
				.texture("0", p.modLoc("block/creative_casing")))
			.build()
			.register();

	public static final BlockEntry<HosePulleyBlock> HOSE_PULLEY = REGISTRATE.block("hose_pulley", HosePulleyBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate(BlockStateGen.horizontalBlockProvider(true))
		.transform(StressConfigDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<ItemDrainBlock> ITEM_DRAIN = REGISTRATE.block("item_drain", ItemDrainBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.addLayer(() -> VertexConsumerProvider::d)
		.blockstate((c, p) -> p.simpleBlock(c.get(), AssetLookup.standardModel(c, p)))
		.simpleItem()
		.register();

	public static final BlockEntry<SpoutBlock> SPOUT = REGISTRATE.block("spout", SpoutBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
		.addLayer(() -> VertexConsumerProvider::d)
		.item(BasinOperatorBlockItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PortableStorageInterfaceBlock> PORTABLE_FLUID_INTERFACE =
		REGISTRATE.block("portable_fluid_interface", PortableStorageInterfaceBlock::forFluids)
			.initialProperties(SharedProperties::softMetal)
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(addMovementBehaviour(new PortableStorageInterfaceMovement()))
			.item()
			.transform(customItemModel())
			.register();

	// Contraptions

	public static final BlockEntry<MechanicalPistonBlock> MECHANICAL_PISTON =
		REGISTRATE.block("mechanical_piston", MechanicalPistonBlock::normal)
			.transform(BuilderTransformers.mechanicalPiston(BlockHalf.DEFAULT))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<MechanicalPistonBlock> STICKY_MECHANICAL_PISTON =
		REGISTRATE.block("sticky_mechanical_piston", MechanicalPistonBlock::sticky)
			.transform(BuilderTransformers.mechanicalPiston(BlockHalf.STICKY))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<PistonExtensionPoleBlock> PISTON_EXTENSION_POLE =
		REGISTRATE.block("piston_extension_pole", PistonExtensionPoleBlock::new)
			.initialProperties(() -> BellBlock.aX)
			.blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(false))
			.simpleItem()
			.register();

	public static final BlockEntry<MechanicalPistonHeadBlock> MECHANICAL_PISTON_HEAD =
		REGISTRATE.block("mechanical_piston_head", MechanicalPistonHeadBlock::new)
			.initialProperties(() -> BellBlock.aX)
			.loot((p, b) -> p.a(b, PISTON_EXTENSION_POLE.get()))
			.blockstate((c, p) -> BlockStateGen.directionalBlockIgnoresWaterlogged(c, p, state -> p.models()
				.getExistingFile(p.modLoc("block/mechanical_piston/" + state.c(MechanicalPistonHeadBlock.TYPE)
					.a() + "/head"))))
			.register();

	public static final BlockEntry<WindmillBearingBlock> WINDMILL_BEARING =
		REGISTRATE.block("windmill_bearing", WindmillBearingBlock::new)
			.transform(BuilderTransformers.bearing("windmill", "gearbox", true))
			.transform(StressConfigDefaults.setCapacity(512.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<MechanicalBearingBlock> MECHANICAL_BEARING =
		REGISTRATE.block("mechanical_bearing", MechanicalBearingBlock::new)
			.transform(BuilderTransformers.bearing("mechanical", "gearbox", false))
			.transform(StressConfigDefaults.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(addMovementBehaviour(new StabilizedBearingMovementBehaviour()))
			.register();

	public static final BlockEntry<ClockworkBearingBlock> CLOCKWORK_BEARING =
		REGISTRATE.block("clockwork_bearing", ClockworkBearingBlock::new)
			.transform(BuilderTransformers.bearing("clockwork", "brass_gearbox", false))
			.transform(StressConfigDefaults.setImpact(4.0))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<PulleyBlock> ROPE_PULLEY = REGISTRATE.block("rope_pulley", PulleyBlock::new)
		.initialProperties(SharedProperties::stone)
		.tag(AllBlockTags.SAFE_NBT.tag)
		.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
		.transform(StressConfigDefaults.setImpact(4.0))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PulleyBlock.RopeBlock> ROPE = REGISTRATE.block("rope", PulleyBlock.RopeBlock::new)
		.initialProperties(SharedProperties.beltMaterial, Fluids.B)
		.tag(AllBlockTags.BRITTLE.tag)
		.properties(p -> p.a(SnowyBlock.h))
		.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
			.getExistingFile(p.modLoc("block/rope_pulley/" + c.getName()))))
		.register();

	public static final BlockEntry<PulleyBlock.MagnetBlock> PULLEY_MAGNET =
		REGISTRATE.block("pulley_magnet", PulleyBlock.MagnetBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.getExistingFile(p.modLoc("block/rope_pulley/" + c.getName()))))
			.register();

	public static final BlockEntry<CartAssemblerBlock> CART_ASSEMBLER =
		REGISTRATE.block("cart_assembler", CartAssemblerBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(PistonBlockEntity.c::b)
			.blockstate(BlockStateGen.cartAssembler())
			.addLayer(() -> VertexConsumerProvider::d)
			.tag(StatHandler.H, AllBlockTags.SAFE_NBT.tag)
			.item(CartAssemblerBlockItem::new)
			.transform(customItemModel())
			.register();

	public static final BlockEntry<ReinforcedRailBlock> REINFORCED_RAIL =
		REGISTRATE.block("reinforced_rail", ReinforcedRailBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(PistonBlockEntity.c::b)
			.blockstate(BlockStateGen.reinforcedRail())
			.addLayer(() -> VertexConsumerProvider::d)
			.tag(StatHandler.H)
			.item()
			.model((c, p) -> p.blockItem(() -> c.getEntry()
				.e(), "/block"))
			.build()
			.register();

	public static final BlockEntry<ControllerRailBlock> CONTROLLER_RAIL =
		REGISTRATE.block("controller_rail", ControllerRailBlock::new)
			.initialProperties(() -> BellBlock.aN)
			.blockstate(new ControllerRailGenerator()::generate)
			.addLayer(() -> VertexConsumerProvider::d)
			.onRegister(CreateRegistrate.blockColors(() -> AllColorHandlers::getRedstonePower))
			.tag(StatHandler.H)
			.item()
			.model((c, p) -> p.generated(c, Create.asResource("block/" + c.getName())))
			.build()
			.register();

	public static final BlockEntry<MinecartAnchorBlock> MINECART_ANCHOR =
		REGISTRATE.block("minecart_anchor", MinecartAnchorBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.getExistingFile(p.modLoc("block/cart_assembler/" + c.getName()))))
			.register();

	public static final BlockEntry<LinearChassisBlock> LINEAR_CHASSIS =
		REGISTRATE.block("linear_chassis", LinearChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(new ChassisCTBehaviour()))
			.lang("Linear Chassis")
			.simpleItem()
			.register();

	public static final BlockEntry<LinearChassisBlock> SECONDARY_LINEAR_CHASSIS =
		REGISTRATE.block("secondary_linear_chassis", LinearChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.linearChassis())
			.onRegister(connectedTextures(new ChassisCTBehaviour()))
			.simpleItem()
			.register();

	public static final BlockEntry<RadialChassisBlock> RADIAL_CHASSIS =
		REGISTRATE.block("radial_chassis", RadialChassisBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(BlockStateGen.radialChassis())
			.item()
			.model((c, p) -> {
				String path = "block/" + c.getName();
				p.cubeColumn(c.getName(), p.modLoc(path + "_side"), p.modLoc(path + "_end"));
			})
			.build()
			.register();

	public static final BlockEntry<DrillBlock> MECHANICAL_DRILL = REGISTRATE.block("mechanical_drill", DrillBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalBlockProvider(true))
		.transform(StressConfigDefaults.setImpact(4.0))
		.onRegister(addMovementBehaviour(new DrillMovementBehaviour()))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<SawBlock> MECHANICAL_SAW = REGISTRATE.block("mechanical_saw", SawBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(new SawGenerator()::generate)
		.transform(StressConfigDefaults.setImpact(4.0))
		.onRegister(addMovementBehaviour(new SawMovementBehaviour()))
		.addLayer(() -> VertexConsumerProvider::d)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<DeployerBlock> DEPLOYER = REGISTRATE.block("deployer", DeployerBlock::new)
		.initialProperties(SharedProperties::stone)
		.blockstate(BlockStateGen.directionalAxisBlockProvider())
		.transform(StressConfigDefaults.setImpact(4.0))
		.onRegister(addMovementBehaviour(new DeployerMovementBehaviour()))
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<PortableStorageInterfaceBlock> PORTABLE_STORAGE_INTERFACE =
		REGISTRATE.block("portable_storage_interface", PortableStorageInterfaceBlock::forItems)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.onRegister(addMovementBehaviour(new PortableStorageInterfaceMovement()))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<HarvesterBlock> MECHANICAL_HARVESTER =
		REGISTRATE.block("mechanical_harvester", HarvesterBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(addMovementBehaviour(new HarvesterMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.addLayer(() -> VertexConsumerProvider::d)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PloughBlock> MECHANICAL_PLOUGH =
		REGISTRATE.block("mechanical_plough", PloughBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(addMovementBehaviour(new PloughMovementBehaviour()))
			.blockstate(BlockStateGen.horizontalBlockProvider(false))
			.simpleItem()
			.register();

	public static final BlockEntry<?>[] SEATS = new BlockEntry<?>[DebugStickItem.values().length];

	static {
		// SEATS
		for (DebugStickItem colour : DebugStickItem.values()) {
			String colourName = colour.a();
			SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
			SEATS[colour.ordinal()] =
				REGISTRATE.block(colourName + "_seat", p -> new SeatBlock(p, colour == DebugStickItem.o))
					.initialProperties(SharedProperties::wooden)
					.onRegister(addMovementBehaviour(movementBehaviour))
					.blockstate((c, p) -> {
						p.simpleBlock(c.get(), p.models()
							.withExistingParent(colourName + "_seat", p.modLoc("block/seat"))
							.texture("1", p.modLoc("block/seat/top_" + colourName))
							.texture("2", p.modLoc("block/seat/side_" + colourName)));
					})
					.recipe((c, p) -> {
						ShapedRecipeJsonFactory.a(c.get())
							.pattern("#")
							.pattern("-")
							.a('#', DyeHelper.getWoolOfDye(colour))
							.a('-', EntityTypeTags.j)
							.criterion("has_wool", RegistrateRecipeProvider.a(EntityTypeTags.field_15507))
							.offerTo(p, Create.asResource("crafting/kinetics/" + c.getName()));
						ShapedRecipeJsonFactory.a(c.get())
							.pattern("#")
							.pattern("-")
							.a('#', DyeHelper.getTagOfDye(colour))
							.a('-', AllItemTags.SEATS.tag)
							.criterion("has_seat", RegistrateRecipeProvider.a(AllItemTags.SEATS.tag))
							.offerTo(p, Create.asResource("crafting/kinetics/" + c.getName() + "_from_other_seat"));
					})
					.onRegisterAfter(HoeItem.class, v -> TooltipHelper.referTo(v, "block.kinetic_api.seat"))
					.tag(AllBlockTags.SEATS.tag)
					.item()
					.tag(AllItemTags.SEATS.tag)
					.build()
					.register();
		}
	}

	public static final BlockEntry<SailBlock> SAIL_FRAME = REGISTRATE.block("sail_frame", p -> SailBlock.frame(p))
		.initialProperties(SharedProperties::wooden)
		.properties(BeetrootsBlock.Properties::b)
		.blockstate(BlockStateGen.directionalBlockProvider(false))
		.tag(AllBlockTags.WINDMILL_SAILS.tag)
		.tag(AllBlockTags.FAN_TRANSPARENT.tag)
		.simpleItem()
		.register();

	public static final BlockEntry<?>[] DYED_SAILS = new BlockEntry<?>[DebugStickItem.values().length];

	public static final BlockEntry<SailBlock> SAIL = REGISTRATE.block("white_sail", p -> SailBlock.withCanvas(p))
		.initialProperties(SharedProperties::wooden)
		.properties(BeetrootsBlock.Properties::b)
		.blockstate(BlockStateGen.directionalBlockProvider(false))
		.tag(AllBlockTags.WINDMILL_SAILS.tag)
		.simpleItem()
		.register();

	static {
		// DYED SAILS
		for (DebugStickItem colour : DebugStickItem.values()) {
			if (colour == DebugStickItem.a) {
				DYED_SAILS[colour.ordinal()] = SAIL;
				continue;
			}
			String colourName = colour.a();
			DYED_SAILS[colour.ordinal()] = REGISTRATE.block(colourName + "_sail", p -> SailBlock.withCanvas(p))
				.properties(BeetrootsBlock.Properties::b)
				.initialProperties(SharedProperties::wooden)
				.blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
					.withExistingParent(colourName + "_sail", p.modLoc("block/white_sail"))
					.texture("0", p.modLoc("block/sail/canvas_" + colourName))))
				.tag(AllBlockTags.WINDMILL_SAILS.tag)
				.tag(AllBlockTags.SAILS.tag)
				.loot((p, b) -> p.a(b, SAIL.get()))
				.register();
		}
	}

	public static final BlockEntry<CasingBlock> ANDESITE_CASING = REGISTRATE.block("andesite_casing", CasingBlock::new)
		.transform(BuilderTransformers.casing(AllSpriteShifts.ANDESITE_CASING))
		.register();

	public static final BlockEntry<CasingBlock> BRASS_CASING = REGISTRATE.block("brass_casing", CasingBlock::new)
		.transform(BuilderTransformers.casing(AllSpriteShifts.BRASS_CASING))
		.register();

	public static final BlockEntry<CasingBlock> COPPER_CASING = REGISTRATE.block("copper_casing", CasingBlock::new)
		.transform(BuilderTransformers.casing(AllSpriteShifts.COPPER_CASING))
		.register();

	public static final BlockEntry<CasingBlock> SHADOW_STEEL_CASING =
		REGISTRATE.block("shadow_steel_casing", CasingBlock::new)
			.transform(BuilderTransformers.casing(AllSpriteShifts.SHADOW_STEEL_CASING))
			.lang("Shadow Casing")
			.register();

	public static final BlockEntry<CasingBlock> REFINED_RADIANCE_CASING =
		REGISTRATE.block("refined_radiance_casing", CasingBlock::new)
			.transform(BuilderTransformers.casing(AllSpriteShifts.REFINED_RADIANCE_CASING))
			.properties(p -> p.a($ -> 12))
			.lang("Radiant Casing")
			.register();

	public static final BlockEntry<MechanicalCrafterBlock> MECHANICAL_CRAFTER =
		REGISTRATE.block("mechanical_crafter", MechanicalCrafterBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.properties(PistonBlockEntity.c::b)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(StressConfigDefaults.setImpact(2.0))
			.onRegister(CreateRegistrate.connectedTextures(new CrafterCTBehaviour()))
			.addLayer(() -> VertexConsumerProvider::d)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SequencedGearshiftBlock> SEQUENCED_GEARSHIFT =
		REGISTRATE.block("sequenced_gearshift", SequencedGearshiftBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.properties(PistonBlockEntity.c::b)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate(new SequencedGearshiftGenerator()::generate)
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<FlywheelBlock> FLYWHEEL = REGISTRATE.block("flywheel", FlywheelBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(PistonBlockEntity.c::b)
		.transform(StressConfigDefaults.setNoImpact())
		.blockstate(new FlywheelGenerator()::generate)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<FurnaceEngineBlock> FURNACE_ENGINE =
		REGISTRATE.block("furnace_engine", FurnaceEngineBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.BRITTLE.tag)
			.blockstate(BlockStateGen.horizontalBlockProvider(true))
			.transform(StressConfigDefaults.setCapacity(1024.0))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<SpeedControllerBlock> ROTATION_SPEED_CONTROLLER =
		REGISTRATE.block("rotation_speed_controller", SpeedControllerBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.transform(StressConfigDefaults.setNoImpact())
			.blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
			.item()
			.transform(customItemModel())
			.register();

	// Logistics

	static {
		REGISTRATE.startSection(AllSections.LOGISTICS);
	}

	public static final BlockEntry<ArmBlock> MECHANICAL_ARM = REGISTRATE.block("mechanical_arm", ArmBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(s -> ConfiguredModel.builder()
				.modelFile(AssetLookup.partialBaseModel(c, p))
				.rotationX(s.c(ArmBlock.CEILING) ? 180 : 0)
				.build()))
		.transform(StressConfigDefaults.setImpact(8.0))
		.item(ArmItem::new)
		.transform(customItemModel())
		.register();

	public static final BlockEntry<AndesiteFunnelBlock> ANDESITE_FUNNEL =
		REGISTRATE.block("andesite_funnel", AndesiteFunnelBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(addMovementBehaviour(FunnelMovementBehaviour.andesite()))
			.transform(BuilderTransformers.funnel("andesite", Create.asResource("block/andesite_casing")))
			.register();

	public static final BlockEntry<AndesiteBeltFunnelBlock> ANDESITE_BELT_FUNNEL =
		REGISTRATE.block("andesite_belt_funnel", AndesiteBeltFunnelBlock::new)
			.initialProperties(SharedProperties::stone)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BeltFunnelGenerator("andesite", new Identifier("block/polished_andesite"))::generate)
			.loot((p, b) -> p.a(b, ANDESITE_FUNNEL.get()))
			.register();

	public static final BlockEntry<BrassFunnelBlock> BRASS_FUNNEL =
		REGISTRATE.block("brass_funnel", BrassFunnelBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.onRegister(addMovementBehaviour(FunnelMovementBehaviour.brass()))
			.transform(BuilderTransformers.funnel("brass", Create.asResource("block/brass_casing")))
			.register();

	public static final BlockEntry<BrassBeltFunnelBlock> BRASS_BELT_FUNNEL =
		REGISTRATE.block("brass_belt_funnel", BrassBeltFunnelBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new BeltFunnelGenerator("brass", Create.asResource("block/brass_block"))::generate)
			.loot((p, b) -> p.a(b, BRASS_FUNNEL.get()))
			.register();

	public static final BlockEntry<BeltTunnelBlock> ANDESITE_TUNNEL =
		REGISTRATE.block("andesite_tunnel", BeltTunnelBlock::new)
			.transform(BuilderTransformers.beltTunnel("andesite", new Identifier("block/polished_andesite")))
			.register();

	public static final BlockEntry<BrassTunnelBlock> BRASS_TUNNEL =
		REGISTRATE.block("brass_tunnel", BrassTunnelBlock::new)
			.transform(BuilderTransformers.beltTunnel("brass", Create.asResource("block/brass_block")))
			.onRegister(connectedTextures(new BrassTunnelCTBehaviour()))
			.register();

	public static final BlockEntry<RedstoneContactBlock> REDSTONE_CONTACT =
		REGISTRATE.block("redstone_contact", RedstoneContactBlock::new)
			.initialProperties(SharedProperties::stone)
			.onRegister(addMovementBehaviour(new ContactMovementBehaviour()))
			.blockstate((c, p) -> p.directionalBlock(c.get(), AssetLookup.forPowered(c, p)))
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<ContentObserverBlock> CONTENT_OBSERVER =
		REGISTRATE.block("content_observer", ContentObserverBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.horizontalBlock(c.get(), AssetLookup.forPowered(c, p)))
			.item()
			.transform(customItemModel("_", "block"))
			.register();

	public static final BlockEntry<StockpileSwitchBlock> STOCKPILE_SWITCH =
		REGISTRATE.block("stockpile_switch", StockpileSwitchBlock::new)
			.initialProperties(SharedProperties::stone)
			.blockstate((c, p) -> p.horizontalBlock(c.get(),
				AssetLookup.withIndicator(c, p, $ -> AssetLookup.standardModel(c, p), StockpileSwitchBlock.INDICATOR)))
			.simpleItem()
			.register();

	public static final BlockEntry<AdjustableCrateBlock> ADJUSTABLE_CRATE =
		REGISTRATE.block("adjustable_crate", AdjustableCrateBlock::new)
			.transform(BuilderTransformers.crate("brass"))
			.register();

	public static final BlockEntry<CreativeCrateBlock> CREATIVE_CRATE =
		REGISTRATE.block("creative_crate", CreativeCrateBlock::new)
			.transform(BuilderTransformers.crate("creative"))
			.tag(AllBlockTags.SAFE_NBT.tag)
			.register();

	public static final BlockEntry<NixieTubeBlock> NIXIE_TUBE = REGISTRATE.block("nixie_tube", NixieTubeBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.a($ -> 5))
		.blockstate(new NixieTubeGenerator()::generate)
		.addLayer(() -> VertexConsumerProvider::f)
		.item()
		.transform(customItemModel())
		.register();

	public static final BlockEntry<RedstoneLinkBlock> REDSTONE_LINK =
		REGISTRATE.block("redstone_link", RedstoneLinkBlock::new)
			.initialProperties(SharedProperties::wooden)
			.tag(AllBlockTags.BRITTLE.tag, AllBlockTags.SAFE_NBT.tag)
			.blockstate(new RedstoneLinkGenerator()::generate)
			.addLayer(() -> VertexConsumerProvider::d)
			.item()
			.transform(customItemModel("_", "transmitter"))
			.register();

	public static final BlockEntry<AnalogLeverBlock> ANALOG_LEVER =
		REGISTRATE.block("analog_lever", AnalogLeverBlock::new)
			.initialProperties(() -> BellBlock.cp)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<PulseRepeaterBlock> PULSE_REPEATER =
		REGISTRATE.block("pulse_repeater", PulseRepeaterBlock::new)
			.initialProperties(() -> BellBlock.cX)
			.blockstate(new PulseRepeaterGenerator()::generate)
			.addLayer(() -> VertexConsumerProvider::d)
			.item()
			.transform(customItemModel("diodes", "pulse_repeater"))
			.register();

	public static final BlockEntry<AdjustableRepeaterBlock> ADJUSTABLE_REPEATER =
		REGISTRATE.block("adjustable_repeater", AdjustableRepeaterBlock::new)
			.initialProperties(() -> BellBlock.cX)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new AdjustableRepeaterGenerator()::generate)
			.item()
			.model(AbstractDiodeGenerator.diodeItemModel(true))
			.build()
			.register();

	public static final BlockEntry<AdjustableRepeaterBlock> ADJUSTABLE_PULSE_REPEATER =
		REGISTRATE.block("adjustable_pulse_repeater", AdjustableRepeaterBlock::new)
			.initialProperties(() -> BellBlock.cX)
			.tag(AllBlockTags.SAFE_NBT.tag)
			.blockstate(new AdjustableRepeaterGenerator()::generate)
			.addLayer(() -> VertexConsumerProvider::d)
			.item()
			.model(AbstractDiodeGenerator.diodeItemModel(true))
			.build()
			.register();

	public static final BlockEntry<PoweredLatchBlock> POWERED_LATCH =
		REGISTRATE.block("powered_latch", PoweredLatchBlock::new)
			.initialProperties(() -> BellBlock.cX)
			.blockstate(new PoweredLatchGenerator()::generate)
			.addLayer(() -> VertexConsumerProvider::d)
			.simpleItem()
			.register();

	public static final BlockEntry<ToggleLatchBlock> POWERED_TOGGLE_LATCH =
		REGISTRATE.block("powered_toggle_latch", ToggleLatchBlock::new)
			.initialProperties(() -> BellBlock.cX)
			.blockstate(new ToggleLatchGenerator()::generate)
			.addLayer(() -> VertexConsumerProvider::d)
			.item()
			.transform(customItemModel("diodes", "latch_off"))
			.register();

	// Materials

	static {
		REGISTRATE.startSection(AllSections.MATERIALS);
	}

	public static final BlockEntry<OxidizingBlock> COPPER_ORE =
		REGISTRATE.block("copper_ore", p -> new OxidizingBlock(p, 1))
			.initialProperties(() -> BellBlock.G)
			.transform(oxidizedBlockstate())
			.tag(Tags.Blocks.ORES)
			.transform(tagBlockAndItem("ores/copper"))
			.tag(Tags.Items.ORES)
			.transform(oxidizedItemModel())
			.register();

	public static final BlockEntry<BeetrootsBlock> ZINC_ORE = REGISTRATE.block("zinc_ore", BeetrootsBlock::new)
		.initialProperties(() -> BellBlock.bE)
		.properties(p -> p.harvestLevel(2)
			.harvestTool(ToolType.PICKAXE)
			.a(SnowyBlock.e))
		.tag(Tags.Blocks.ORES)
		.transform(tagBlockAndItem("ores/zinc"))
		.tag(Tags.Items.ORES)
		.build()
		.register();

	public static final BlockEntry<OxidizingBlock> COPPER_BLOCK =
		REGISTRATE.block("copper_block", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> BellBlock.bF)
			.tag(Tags.Blocks.STORAGE_BLOCKS)
			.tag(AllBlockTags.BEACON_BASE_BLOCKS.tag)
			.transform(tagBlockAndItem("storage_blocks/copper"))
			.tag(Tags.Items.STORAGE_BLOCKS)
			.transform(oxidizedItemModel())
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<OxidizingBlock> COPPER_SHINGLES =
		REGISTRATE.block("copper_shingles", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> BellBlock.bF)
			.item()
			.transform(oxidizedItemModel())
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<OxidizingBlock> COPPER_TILES =
		REGISTRATE.block("copper_tiles", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> BellBlock.bF)
			.item()
			.transform(oxidizedItemModel())
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<BeetrootsBlock> ZINC_BLOCK = REGISTRATE.block("zinc_block", p -> new BeetrootsBlock(p))
		.initialProperties(() -> BellBlock.bF)
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(AllBlockTags.BEACON_BASE_BLOCKS.tag)
		.transform(tagBlockAndItem("storage_blocks/zinc"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.register();

	public static final BlockEntry<BeetrootsBlock> BRASS_BLOCK = REGISTRATE.block("brass_block", p -> new BeetrootsBlock(p))
		.initialProperties(() -> BellBlock.bF)
		.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
			.cubeAll(c.getName(), p.modLoc("block/brass_storage_block"))))
		.tag(Tags.Blocks.STORAGE_BLOCKS)
		.tag(AllBlockTags.BEACON_BASE_BLOCKS.tag)
		.transform(tagBlockAndItem("storage_blocks/brass"))
		.tag(Tags.Items.STORAGE_BLOCKS)
		.build()
		.register();

	// Load this class

	public static void register() {}

}

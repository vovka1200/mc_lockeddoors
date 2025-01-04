package com.unitedarts.lockeddoors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class LockableDoor extends DoorBlock {

	private static final Logger LOGGER = LogUtils.getLogger();
	public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

	public LockableDoor(BlockSetType blockSetType, BlockBehaviour.Properties properties) {
		super(blockSetType, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LOCKED, true));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LOCKED);
	}

	protected boolean isLocked(BlockState state) {
		return state.getValue(LOCKED);
	}

	protected BlockState setLocked(Entity entity, Level world, BlockState state, BlockPos pos, boolean locked) {
		BlockState newState = state.setValue(LOCKED, locked);
		world.setBlockAndUpdate(pos, newState);
		LOGGER.info("setLocked(): Door is {}", newState);
		return newState;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult hit) {
		String itemInHand = player.getMainHandItem().getItem().toString();
		String blockId = state.getBlock().getDescriptionId();
		LOGGER.info("useWithoutItem(): {} opened={}, locked={}, in hand {}", blockId, state.getValue(OPEN),
				state.getValue(LOCKED), itemInHand);
		if (isOpen(state)) {
			return super.useWithoutItem(state, world, pos, player, hit);
		} else {
			LOGGER.info("useWithoutItem(): blockId {}", itemInHand.replaceFirst("_key", ""));
			if (blockId.endsWith(itemInHand.replaceFirst("_key", ""))) {
				if (isLocked(state)) {
					state = setLocked(player, world, state, pos, false);
				} else {
					state = setLocked(player, world, state, pos, true);
				}
				return InteractionResult.SUCCESS;
			} else {
				if (isLocked(state)) {
					return InteractionResult.SUCCESS;
				} else {
					return super.useWithoutItem(state, world, pos, player, hit);
				}
			}
		}
	}
}

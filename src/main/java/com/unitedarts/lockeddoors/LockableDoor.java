package com.unitedarts.lockeddoors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
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

/**
 * 
 */
public class LockableDoor extends DoorBlock {

	private static final Logger LOGGER = LogUtils.getLogger();
	public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

	/**
	 * 
	 * @param blockSetType
	 * @param properties
	 */
	public LockableDoor(BlockSetType blockSetType, BlockBehaviour.Properties properties) {
		super(blockSetType, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LOCKED, true));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LOCKED);
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	protected boolean isLocked(BlockState state) {
		return state.getValue(LOCKED);
	}

	/**
	 * 
	 * @param entity
	 * @param world
	 * @param state
	 * @param pos
	 * @param locked
	 * @return
	 */
	protected BlockState setLocked(Entity entity, Level world, BlockState state, BlockPos pos, boolean locked) {
		BlockState newState = state.setValue(LOCKED, locked);
		world.setBlockAndUpdate(pos, newState);
		LOGGER.debug("door state {}", newState);
		return newState;
	}

	/**
	 * 
	 * @param state
	 * @param player
	 * @return
	 */
	protected boolean hasKey(BlockState state, Player player) {
		String itemInHand = player.getMainHandItem().getItem().toString();
		String blockId = state.getBlock().getDescriptionId();
		LOGGER.debug("{} opened={}, locked={}, in hand {}", blockId, state.getValue(OPEN), state.getValue(LOCKED),
				itemInHand);
		return itemInHand.contains("key") && blockId.endsWith(itemInHand.replaceFirst("_key", ""));
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult hit) {
		LOGGER.debug("door opened={}, locked={}", state.getValue(OPEN), state.getValue(LOCKED));
		if (isOpen(state)) {
			return super.useWithoutItem(state, world, pos, player, hit);
		} else {
			if (hasKey(state, player)) {
				if (isLocked(state)) {
					state = setLocked(player, world, state, pos, false);
				} else {
					state = setLocked(player, world, state, pos, true);
				}
				return InteractionResult.SUCCESS;
			} else {
				if (isLocked(state)) {
					world.playLocalSound(pos, LockedDoors.LOCKED_DOOR_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f,
							false);
					return InteractionResult.SUCCESS;
				} else {
					return super.useWithoutItem(state, world, pos, player, hit);
				}
			}
		}
	}
}

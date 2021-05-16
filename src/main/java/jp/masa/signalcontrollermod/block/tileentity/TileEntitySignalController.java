package jp.masa.signalcontrollermod.block.tileentity;

import jp.masa.signalcontrollermod.gui.signalcontroller.SignalType;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.electric.TileEntitySignal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TileEntitySignalController extends TileEntityCustom implements ITickable {
    private SignalType signalType;
    private List<BlockPos> nextSignal;
    private List<BlockPos> displayPos;
    private boolean last;
    private boolean repeat;
    private boolean reducedSpeed;

    public TileEntitySignalController() {
        this.signalType = SignalType.signal3;
        this.nextSignal = new ArrayList<>();
        this.nextSignal.add(BlockPos.ORIGIN);
        this.displayPos = new ArrayList<>();
        this.displayPos.add(BlockPos.ORIGIN);
        this.last = false;
        this.repeat = false;
        this.reducedSpeed = false;
    }

    @Override
    public void update() {
        World world = this.getWorld();
        if (!world.isRemote) {
            int MAXSIGNALLEVEL = 6;
            List<Integer> nextSignalList = new ArrayList<>();

            for (BlockPos blockPos : this.nextSignal) {
                Object nextSignal = this.getSignal(world, blockPos);

                if (nextSignal instanceof Integer) {
                    nextSignalList.add((int) nextSignal);
                }
            }
            int nextSignalLevel = (this.last) ? 1 : nextSignalList.stream().mapToInt(v -> v).max().orElse(0);

            // RS入力(停止現示)
            boolean isRSPowered = world.isBlockIndirectlyGettingPowered(this.pos) > 0; //レッドストーン確認

            //表示する信号機の制御
            //変化したときだけ変更するようにすることで負荷を減らすこと
            int signalLevel = (this.repeat && (3 <= nextSignalLevel && nextSignalLevel <= 4)) ? nextSignalLevel : this.signalType.upSignalLevel(nextSignalLevel);
            Object currentSignal;
            if (signalLevel > MAXSIGNALLEVEL) signalLevel = MAXSIGNALLEVEL;
            if (isRSPowered) signalLevel = 1;

            for (BlockPos blockPos : this.displayPos) {
                currentSignal = getSignal(world, blockPos);
                if (currentSignal != null && (int) currentSignal != signalLevel) {
                    setSignal(world, blockPos, signalLevel);
                }
            }
        }
    }

    private Object getSignal(World world, BlockPos blockPos) {
        TileEntity tileEntity = world.getTileEntity(blockPos);
        if (tileEntity instanceof TileEntitySignal)
            return NGTUtil.getField(TileEntitySignal.class, tileEntity, "signalLevel");
        return null;
    }

    private void setSignal(World world, BlockPos blockPos, int level) {
        TileEntity tileEntity = world.getTileEntity(blockPos);
        if (tileEntity instanceof TileEntitySignal) {
            ((TileEntitySignal) tileEntity).setElectricity(blockPos.getX(), blockPos.getY(), blockPos.getZ(), level);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.signalType = SignalType.getType(nbt.getString("signalType"));
        this.last = nbt.getBoolean("last");
        this.repeat = nbt.getBoolean("repeat");
        this.reducedSpeed = nbt.getBoolean("reducedSpeed");
        // nextSignal
        NBTTagList nextSignalList = nbt.getTagList("nextSignalList", 10);
        this.nextSignal.clear();
        for (int i = 0; i < nextSignalList.tagCount(); i++) {
            NBTTagCompound tag = nextSignalList.getCompoundTagAt(i);
            BlockPos blockPos = BlockPos.fromLong(tag.getLong("pos"));
            this.nextSignal.add(blockPos);
        }
        // displayPos
        NBTTagList displayPosList = nbt.getTagList("displayPosList", 10);
        this.displayPos.clear();
        for (int i = 0; i < displayPosList.tagCount(); i++) {
            NBTTagCompound tag = displayPosList.getCompoundTagAt(i);
            BlockPos blockPos = BlockPos.fromLong(tag.getLong("pos"));
            this.displayPos.add(blockPos);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("signalType", this.signalType.toString());
        nbt.setBoolean("last", this.last);
        nbt.setBoolean("repeat", this.repeat);
        nbt.setBoolean("reducedSpeed", this.reducedSpeed);
        // nextSignal
        NBTTagList nextSignalList = new NBTTagList();
        this.nextSignal.forEach(blockPos -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("pos", blockPos.toLong());
            nextSignalList.appendTag(tag);
        });
        nbt.setTag("nextSignalList", nextSignalList);
        // displayPos
        NBTTagList displayPosList = new NBTTagList();
        this.displayPos.forEach(blockPos -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("pos", blockPos.toLong());
            displayPosList.appendTag(tag);
        });
        nbt.setTag("displayPosList", displayPosList);
        return nbt;
    }

    public SignalType getSignalType() {
        return (this.signalType == null) ? this.signalType = SignalType.signal3 : this.signalType;
    }

    public void setSignalType(SignalType signalType) {
        this.signalType = signalType;
    }

    public List<BlockPos> getNextSignal() {
        return nextSignal;
    }

    public void setNextSignal(List<BlockPos> nextSignal) {
        this.nextSignal = nextSignal;
    }

    public boolean addNextSignal(BlockPos nextSignalPos) {
        for (BlockPos pos : this.nextSignal) {
            if (pos.equals(nextSignalPos)) {
                return false;
            } else if (nextSignalPos.equals(BlockPos.ORIGIN)) {
                return true;
            }
        }
        if (this.nextSignal.get(0).equals(BlockPos.ORIGIN)) {
            this.nextSignal.remove(0);
        }
        this.nextSignal.add(nextSignalPos);
        return true;
    }

    public List<BlockPos> getDisplayPos() {
        return displayPos;
    }

    public void setDisplayPos(List<BlockPos> displayPos) {
        this.displayPos = displayPos;
    }

    public boolean addDisplayPos(BlockPos displayPos) {
        for (BlockPos pos : this.displayPos) {
            if (pos.equals(displayPos)) {
                return false;
            } else if (displayPos.equals(BlockPos.ORIGIN)) {
                return true;
            }
        }
        if (this.displayPos.get(0).equals(BlockPos.ORIGIN)) {
            this.displayPos.remove(0);
        }
        this.displayPos.add(displayPos);
        return true;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isReducedSpeed() {
        return reducedSpeed;
    }

    public void setReducedSpeed(boolean reducedSpeed) {
        this.reducedSpeed = reducedSpeed;
    }
}

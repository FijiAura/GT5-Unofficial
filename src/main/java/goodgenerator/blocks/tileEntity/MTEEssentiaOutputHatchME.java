package goodgenerator.blocks.tileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import goodgenerator.util.ItemRefer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.api.grid.IEssentiaGrid;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;

public class MTEEssentiaOutputHatchME extends MTEEssentiaOutputHatch implements IActionHost, IGridProxyable {

    private AENetworkProxy gridProxy = null;
    private IMEEssentiaMonitor monitor = null;
    private final MachineSource asMachineSource = new MachineSource(this);
    public long mTickTimer = 0;

    @Override
    public void updateEntity() {
        AENetworkProxy gp = getProxy();
        if (mTickTimer++ == 0 && gp != null) {
            gp.onReady();
        }
        super.updateEntity();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.invalidateAE();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.onChunkUnloadAE();
    }

    @Override
    public void readFromNBT(NBTTagCompound aNBT) {
        super.readFromNBT(aNBT);
        getProxy().readFromNBT(aNBT);
    }

    @Override
    public void writeToNBT(NBTTagCompound aNBT) {
        super.writeToNBT(aNBT);
        getProxy().writeToNBT(aNBT);
    }

    void onChunkUnloadAE() {
        getProxy().onChunkUnload();
    }

    void invalidateAE() {
        getProxy().invalidate();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection forgeDirection) {
        return getProxy().getNode();
    }

    @Override
    public void gridChanged() {}

    @Override
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {}

    @Override
    public AENetworkProxy getProxy() {
        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", ItemRefer.Essentia_Output_Hatch_ME.get(1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        }
        return this.gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public IGridNode getActionableNode() {
        return getProxy().getNode();
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        return false;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, ForgeDirection side) {
        return this.addEssentia(aspect, amount, side, Actionable.MODULATE);
    }

    public int addEssentia(Aspect aspect, int amount, ForgeDirection side, Actionable mode) {
        long rejectedAmount = amount;
        if (this.getEssentiaMonitor()) {
            rejectedAmount = this.monitor.injectEssentia(aspect, amount, mode, this.getMachineSource(), true);
        }

        long acceptedAmount = (long) amount - rejectedAmount;
        return (int) acceptedAmount;
    }

    protected boolean getEssentiaMonitor() {
        IMEEssentiaMonitor essentiaMonitor = null;
        IGrid grid = null;
        IGridNode node = this.getProxy()
            .getNode();

        if (node != null) {
            grid = node.getGrid();
            if (grid != null) essentiaMonitor = grid.getCache(IEssentiaGrid.class);
        }
        this.monitor = essentiaMonitor;
        return (this.monitor != null);
    }

    public MachineSource getMachineSource() {
        return this.asMachineSource;
    }
}

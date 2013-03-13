package net.minecraft.server;

import java.util.List;

public class EntityBoat extends Entity {

    private boolean a;
    private double b;
    private int c;
    private double d;
    private double e;
    private double f;
    private double g;
    private double h;

    public EntityBoat(World world) {
        super(world);
        this.a = true;
        this.b = 0.07D;
        this.m = true;
        this.a(1.5F, 0.6F);
        this.height = this.length / 2.0F;
    }

    protected boolean f_() {
        return false;
    }

    protected void a() {
        this.datawatcher.a(17, new Integer(0));
        this.datawatcher.a(18, new Integer(1));
        this.datawatcher.a(19, new Integer(0));
    }

    public AxisAlignedBB g(Entity entity) {
        return entity.boundingBox;
    }

    public AxisAlignedBB D() {
        return this.boundingBox;
    }

    public boolean L() {
        return true;
    }

    public EntityBoat(World world, double d0, double d1, double d2) {
        this(world);
        this.setPosition(d0, d1 + (double) this.height, d2);
        this.motX = 0.0D;
        this.motY = 0.0D;
        this.motZ = 0.0D;
        this.lastX = d0;
        this.lastY = d1;
        this.lastZ = d2;
    }

    public double W() {
        return (double) this.length * 0.0D - 0.30000001192092896D;
    }

    public boolean damageEntity(DamageSource damagesource, int i) {
        if (this.isInvulnerable()) {
            return false;
        } else if (!this.world.isStatic && !this.dead) {
            this.h(-this.h());
            this.b(10);
            this.setDamage(this.getDamage() + i * 10);
            this.J();
            boolean flag = damagesource.getEntity() instanceof EntityHuman && ((EntityHuman) damagesource.getEntity()).abilities.canInstantlyBuild;

            if (flag || this.getDamage() > 40) {
                if (this.passenger != null) {
                    this.passenger.mount(this);
                }

                if (!flag) {
                    this.a(Item.BOAT.id, 1, 0.0F);
                }

                this.die();
            }

            return true;
        } else {
            return true;
        }
    }

    public boolean K() {
        return !this.dead;
    }

    public void l_() {
        super.l_();
        if (this.g() > 0) {
            this.b(this.g() - 1);
        }

        if (this.getDamage() > 0) {
            this.setDamage(this.getDamage() - 1);
        }

        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        byte b0 = 5;
        double d0 = 0.0D;

        for (int i = 0; i < b0; ++i) {
            double d1 = this.boundingBox.b + (this.boundingBox.e - this.boundingBox.b) * (double) (i + 0) / (double) b0 - 0.125D;
            double d2 = this.boundingBox.b + (this.boundingBox.e - this.boundingBox.b) * (double) (i + 1) / (double) b0 - 0.125D;
            AxisAlignedBB axisalignedbb = AxisAlignedBB.a().a(this.boundingBox.a, d1, this.boundingBox.c, this.boundingBox.d, d2, this.boundingBox.f);

            if (this.world.b(axisalignedbb, Material.WATER)) {
                d0 += 1.0D / (double) b0;
            }
        }

        double d3 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
        double d4;
        double d5;

        if (d3 > 0.26249999999999996D) {
            d4 = Math.cos((double) this.yaw * 3.141592653589793D / 180.0D);
            d5 = Math.sin((double) this.yaw * 3.141592653589793D / 180.0D);

            for (int j = 0; (double) j < 1.0D + d3 * 60.0D; ++j) {
                double d6 = (double) (this.random.nextFloat() * 2.0F - 1.0F);
                double d7 = (double) (this.random.nextInt(2) * 2 - 1) * 0.7D;
                double d8;
                double d9;

                if (this.random.nextBoolean()) {
                    d8 = this.locX - d4 * d6 * 0.8D + d5 * d7;
                    d9 = this.locZ - d5 * d6 * 0.8D - d4 * d7;
                    this.world.addParticle("splash", d8, this.locY - 0.125D, d9, this.motX, this.motY, this.motZ);
                } else {
                    d8 = this.locX + d4 + d5 * d6 * 0.7D;
                    d9 = this.locZ + d5 - d4 * d6 * 0.7D;
                    this.world.addParticle("splash", d8, this.locY - 0.125D, d9, this.motX, this.motY, this.motZ);
                }
            }
        }

        double d10;
        double d11;

        if (this.world.isStatic && this.a) {
            if (this.c > 0) {
                d4 = this.locX + (this.d - this.locX) / (double) this.c;
                d5 = this.locY + (this.e - this.locY) / (double) this.c;
                d10 = this.locZ + (this.f - this.locZ) / (double) this.c;
                d11 = MathHelper.g(this.g - (double) this.yaw);
                this.yaw = (float) ((double) this.yaw + d11 / (double) this.c);
                this.pitch = (float) ((double) this.pitch + (this.h - (double) this.pitch) / (double) this.c);
                --this.c;
                this.setPosition(d4, d5, d10);
                this.b(this.yaw, this.pitch);
            } else {
                d4 = this.locX + this.motX;
                d5 = this.locY + this.motY;
                d10 = this.locZ + this.motZ;
                this.setPosition(d4, d5, d10);
                if (this.onGround) {
                    this.motX *= 0.5D;
                    this.motY *= 0.5D;
                    this.motZ *= 0.5D;
                }

                this.motX *= 0.9900000095367432D;
                this.motY *= 0.949999988079071D;
                this.motZ *= 0.9900000095367432D;
            }
        } else {
            if (d0 < 1.0D) {
                d4 = d0 * 2.0D - 1.0D;
                this.motY += 0.03999999910593033D * d4;
            } else {
                if (this.motY < 0.0D) {
                    this.motY /= 2.0D;
                }

                this.motY += 0.007000000216066837D;
            }

            if (this.passenger != null) {
                this.motX += this.passenger.motX * this.b;
                this.motZ += this.passenger.motZ * this.b;
            }

            d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
            if (d4 > 0.35D) {
                d5 = 0.35D / d4;
                this.motX *= d5;
                this.motZ *= d5;
                d4 = 0.35D;
            }

            if (d4 > d3 && this.b < 0.35D) {
                this.b += (0.35D - this.b) / 35.0D;
                if (this.b > 0.35D) {
                    this.b = 0.35D;
                }
            } else {
                this.b -= (this.b - 0.07D) / 35.0D;
                if (this.b < 0.07D) {
                    this.b = 0.07D;
                }
            }

            if (this.onGround) {
                this.motX *= 0.5D;
                this.motY *= 0.5D;
                this.motZ *= 0.5D;
            }

            this.move(this.motX, this.motY, this.motZ);
            if (this.positionChanged && d3 > 0.2D) {
                if (!this.world.isStatic) {
                    this.die();

                    int k;

                    for (k = 0; k < 3; ++k) {
                        this.a(Block.WOOD.id, 1, 0.0F);
                    }

                    for (k = 0; k < 2; ++k) {
                        this.a(Item.STICK.id, 1, 0.0F);
                    }
                }
            } else {
                this.motX *= 0.9900000095367432D;
                this.motY *= 0.949999988079071D;
                this.motZ *= 0.9900000095367432D;
            }

            this.pitch = 0.0F;
            d5 = (double) this.yaw;
            d10 = this.lastX - this.locX;
            d11 = this.lastZ - this.locZ;
            if (d10 * d10 + d11 * d11 > 0.001D) {
                d5 = (double) ((float) (Math.atan2(d11, d10) * 180.0D / 3.141592653589793D));
            }

            double d12 = MathHelper.g(d5 - (double) this.yaw);

            if (d12 > 20.0D) {
                d12 = 20.0D;
            }

            if (d12 < -20.0D) {
                d12 = -20.0D;
            }

            this.yaw = (float) ((double) this.yaw + d12);
            this.b(this.yaw, this.pitch);
            if (!this.world.isStatic) {
                List list = this.world.getEntities(this, this.boundingBox.grow(0.20000000298023224D, 0.0D, 0.20000000298023224D));
                int l;

                if (list != null && !list.isEmpty()) {
                    for (l = 0; l < list.size(); ++l) {
                        Entity entity = (Entity) list.get(l);

                        if (entity != this.passenger && entity.L() && entity instanceof EntityBoat) {
                            entity.collide(this);
                        }
                    }
                }

                for (l = 0; l < 4; ++l) {
                    int i1 = MathHelper.floor(this.locX + ((double) (l % 2) - 0.5D) * 0.8D);
                    int j1 = MathHelper.floor(this.locZ + ((double) (l / 2) - 0.5D) * 0.8D);

                    for (int k1 = 0; k1 < 2; ++k1) {
                        int l1 = MathHelper.floor(this.locY) + k1;
                        int i2 = this.world.getTypeId(i1, l1, j1);

                        if (i2 == Block.SNOW.id) {
                            this.world.setAir(i1, l1, j1);
                        } else if (i2 == Block.WATER_LILY.id) {
                            this.world.setAir(i1, l1, j1, true);
                        }
                    }
                }

                if (this.passenger != null && this.passenger.dead) {
                    this.passenger = null;
                }
            }
        }
    }

    public void U() {
        if (this.passenger != null) {
            double d0 = Math.cos((double) this.yaw * 3.141592653589793D / 180.0D) * 0.4D;
            double d1 = Math.sin((double) this.yaw * 3.141592653589793D / 180.0D) * 0.4D;

            this.passenger.setPosition(this.locX + d0, this.locY + this.W() + this.passenger.V(), this.locZ + d1);
        }
    }

    protected void b(NBTTagCompound nbttagcompound) {}

    protected void a(NBTTagCompound nbttagcompound) {}

    public boolean a_(EntityHuman entityhuman) {
        if (this.passenger != null && this.passenger instanceof EntityHuman && this.passenger != entityhuman) {
            return true;
        } else {
            if (!this.world.isStatic) {
                entityhuman.mount(this);
            }

            return true;
        }
    }

    public void setDamage(int i) {
        this.datawatcher.watch(19, Integer.valueOf(i));
    }

    public int getDamage() {
        return this.datawatcher.getInt(19);
    }

    public void b(int i) {
        this.datawatcher.watch(17, Integer.valueOf(i));
    }

    public int g() {
        return this.datawatcher.getInt(17);
    }

    public void h(int i) {
        this.datawatcher.watch(18, Integer.valueOf(i));
    }

    public int h() {
        return this.datawatcher.getInt(18);
    }
}

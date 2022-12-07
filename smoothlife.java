import java.util.Random;
import java.awt.Graphics;
import java.awt.Color;

public class smoothlife {
    int height;
    int width;
    double[][] state;
    double[][] mem;
    int ri;
    int ra;
    double b1;
    double b2;
    double d1;
    double d2;
    double am;
    double an;
    double dt;
    int plot_size;
    double[][] ker_d;
    double[][] ker_r;
    Color[] arr_clr;
    Random rand=new Random();
    smoothlife(int ri,int ra,double b1,double b2,
    double d1,double d2,double am,double an,
    double dt,int h,int w,int ps){
        this.ri=ri;this.ra=ra;
        this.b1=b1;this.b2=b2;
        this.d1=d1;this.d2=d2;
        this.am=am;this.an=an;
        this.dt=dt;
        height = h;
        width = w;
        plot_size=ps;
        state=zero_arr_d(height,width);
        mem=zero_arr_d(height,width);
        ker_d=make_disk_ker(this.ri);
        ker_r=make_ring_ker(this.ri, this.ra);
        arr_clr=new Color[256];
        for(int i=0;i<256;i++){
            arr_clr[i]=new Color(i,i,i);
        }
    }

    double[][] zero_arr_d(int h,int w){
        double[][] arr=new double[h][w];
        for(int i=0;i<h;i++){
            for(int j=0;j<w;j++){
                arr[i][j]=0;
            }
        }
        return arr;
    }

    void rand_cir_arr(int num_cir){
        int r;
        int x;
        int y;
        int dx;
        int dy;
        int[] pos;
        for(int i=0;i<num_cir;i++){
            r=rand.nextInt(7,13);
            x=rand.nextInt(0,width);
            y=rand.nextInt(0,height);
            for(int j=0;j<=2*r+1;j++){
                for(int k=0;k<=2*r+1;k++){
                    dx=k-r;dy=j-r;
                    if(dx*dx+dy*dy<=r*r){
                        pos=periodic_pos(x,y,dx,dy);
                        state[pos[1]][pos[0]]=1.0;
                    }
                }
            }
        }
    }

    double[][] make_disk_ker(int ri){
        double b=1;
        double ux;
        double uy;
        double l;
        double s=0;
        double ds=0;
        double[][] ker=new double[2*ri+1][2*ri+1];
        for(int i=0;i<2*ri+1;i++){
            for(int j=0;j<2*ri+1;j++){
                ux=-ri+j;
                uy=-ri+i;
                l=Math.pow(ux*ux+uy*uy,0.5);
                if(l<-b*0.5+ri){
                    ds=1;
                }else if(l>b*0.5+ri){
                    ds=0;
                }else{
                    ds=(b*0.5+ri-l)/b;
                }
                ker[i][j]=ds;
                s+=ds;
            }
        }
        for(int i=0;i<2*ri+1;i++){
            for(int j=0;j<2*ri+1;j++){
                ker[i][j]=ker[i][j]/(s+0.0001);
            }
        }
        return ker;
    }

    double[][] make_ring_ker(int ri,int ra){
        double b=1;
        double ux;
        double uy;
        double l;
        double s=0;
        double ds=0;
        double[][] ker=new double[2*ra+1][2*ra+1];
        for(int i=0;i<ra*2+1;i++){
            for(int j=0;j<ra*2+1;j++){
                ux=-ra+j;
                uy=-ra+i;
                l=Math.pow(ux*ux+uy*uy,0.5);
                if(l<-b*0.5+ri){
                    ds=0;
                }else if(l>=-b*0.5+ri
                &&l<=b*0.5+ri){
                    ds=(b*0.5+ri-l)/b;
                }else if(l>b*0.5+ri&&l<-b*0.5+ra){
                    ds=1;
                }else if(l>=-b*0.5+ra&&
                l<=b*0.5+ra){
                    ds=(b*0.5+ra-l)/b;
                }else if(l>b*0.5+ra){
                    ds=0;
                }
                ker[i][j]=ds;
                s+=ds;
            }
        }
        for(int i=0;i<ra*2+1;i++){
            for(int j=0;j<ra*2+1;j++){
                ker[i][j]=ker[i][j]/(s+0.0001);
            }
        }
        return ker;
    }

    int[] periodic_pos(int x,int y,int dx,int dy){
        int h=this.height;
        int w=this.width;
        int nx=x+dx;
        int ny=y+dy;
        if(nx<0){nx=nx+w;}
        else if(nx>w-1){nx=nx-w;}
        if(ny<0){ny=ny+h;}
        else if(ny>h-1){ny=ny-h;}
        int[] pos=new int[]{nx,ny};
        return pos;

    }

    double calc_conv_p(double[][] arr,double[][] ker,int rk,int x,int y){
        double cv=0;
        int[] pos;
        for(int i=0;i<rk*2+1;i++){
            for(int j=0;j<rk*2+1;j++){
                pos=periodic_pos(x,y,j-rk,i-rk);
                cv=cv+ker[i][j]*arr[pos[1]][pos[0]];
            }
        }
        return cv;
    }

    void rand_state(){
        int t=5;
        for (int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                if(i>height/t&&i<height/t*2
                &&j>width/t&&j<width/t*2){
                state[i][j]=Math.pow(
                    rand.nextDouble(0.0,1.0),2);
                }
            }
        }
    }

    double sig(double x, double a,double alpha){
        double ex=Math.exp(-(x-a)*4/alpha);
        return ((double)1/(1+ex));
    }

    double sig_mul(double x,double a,double b,double alpha){
        return sig(x,a,alpha)*((double)1-sig(x,b,alpha));
    }

    double sigM(double x,double y,double m){
        double minis=sig(m,0.5,am);
        return x*(1-minis)+y*minis;
    }

    double s(double n,double m){
        return (sig_mul(n,sigM(b1,d1,m),sigM(b2,d2,m),an));
    }

    void calc_update(){
        double m;
        double n;
        double S;
        int h=this.height;
        int w=this.width;
        for(int i=0;i<h;i++){
            for(int j=0;j<w;j++){
                m=calc_conv_p(state,ker_d,ri,j,i);
                n=calc_conv_p(state,ker_r,ra,j,i);
                S=s(n,m);
                mem[i][j]=S;//state[i][j]+dt*(2*S-1);
                //if(S<-1){printd(S);}
                if(mem[i][j]<0){
                    mem[i][j]=0;
                }else if(mem[i][j]>1){
                    mem[i][j]=1;
                }

            }
        }
    }

    void state_update(){
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                state[i][j]=mem[i][j];
            }
        }
    }

    void printd(double x){
        System.out.printf("%f\n",x);
    }

    void disp_life(Graphics g){
        int m=plot_size;

        double c;
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                c=255*state[i][j];
                g.setColor(arr_clr[(int)c]);
                g.fillRect(j*m,i*m,m,m);
            }
        }
        //plot_kernel(g, ker_r,ra);
    }

    void plot_kernel(Graphics g,double[][] ker,int r){
        double c;
        int m=plot_size;
        double d=0;
        for(int i=0;i<2*r+1;i++){
            for(int j=0;j<2*r+1;j++){
                c=255*ker[i][j];
                d+=ker[i][j];
                System.out.printf("%f ",ker[i][j]);
                g.setColor(arr_clr[(int)c]);
                g.fillRect(j*m*2,i*m*2,m*2,m*2);
            }
            System.out.printf("\n");
        }
        printd(d);
    }
}

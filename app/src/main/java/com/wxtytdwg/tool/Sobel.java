package com.wxtytdwg.tool;

import android.graphics.*;

public class Sobel
{
	private Bitmap temp;
    private int[] mmap;
	int w,h;
	int bx,by,brx,bry,blx,bly,px,py,cx,cy;
	double gh3=Math.sqrt(3);
	public final int peoplecolor=0xff5a5283;
	public final int people_high=158;
	public final int center_color=0xfff5f5f5;
	
	public int red(int color){
		return color>>16&0xFF;
	}
	public int green(int color){
		return color>>8&0xFF;
	}
	public int blue(int color){
		return color&0xFF;
	}
	
	public int[] getblocktop(){
		int board_x = 0;
		int board_y = 0;
		int last_pixel;
		for(int i = 0; i < h/2; i++){
			last_pixel = getPixel(0, i);
			if(board_x>0|| board_y>0){
				break;
			}
			int board_x_sum = 0;
			int board_x_c = 0;

			for(int j = 0; j < w; j++){
				if(j==px-40)j+=80;
				int pixel = getPixel(j, i);
				// 修掉圆顶的时候一条线导致的小 bug，这个颜色判断应该 OK，暂时不提出来
				//System.out.println(red(pixel) - red(last_pixel));
				if(Math.abs(red(pixel) - red(last_pixel)) + Math.abs(blue(pixel) - blue(last_pixel)) + Math.abs(green(pixel) - green(last_pixel)) > 10){
					board_x_sum += j;
					board_x_c += 1;
				}
			}
			if(board_x_sum>0){
				board_x = board_x_sum / board_x_c;
				board_y=i;
			}
		}
		bx=board_x;by=board_y;
		return new int[]{board_x,board_y};
	}
	public int[] getblockright(){
		double x=bx+10*gh3,y=by+10;
		int lx=0,ly=0;
		do{
			lx=ly=0;
			int board_x_sum=0,board_x_c=0;
			y++;x+=gh3;
			int last_pixel = getPixel(0, (int)y);
			for(int i=-2;i<8;i++){
				if(Math.round(x)>w){
					brx=bry=0;
					return null;
				}
				int pixel = getPixel((int)Math.round(x)+i,(int)y);
				// 修掉圆顶的时候一条线导致的小 bug，这个颜色判断应该 OK，暂时不提出来
				//System.out.println(red(pixel) - red(last_pixel));
				if(Math.abs(red(pixel) - red(last_pixel)) + Math.abs(blue(pixel) - blue(last_pixel)) + Math.abs(green(pixel) - green(last_pixel)) > 10){
					board_x_sum += Math.round(x+i);
					board_x_c += 1;
				}
			}
			if(board_x_sum>0){
				lx = board_x_sum / board_x_c;
				ly=(int)y;
			}
		}while(lx!=0||ly!=0);
		brx=(int)x;bry=(int)y-2;
		return new int[]{(int)x,(int)y};
	}
	public int[] getblockleft(){
		double x=bx-10*gh3,y=by+10;
		int lx=0,ly=0;
		do{
			lx=ly=0;
			int board_x_sum=0,board_x_c=0;
			y++;x-=gh3;
			int last_pixel = getPixel(0, (int)y);
			for(int i=-8;i<2;i++){
				int pixel = getPixel((int)Math.round(x)+i,(int)y);
				// 修掉圆顶的时候一条线导致的小 bug，这个颜色判断应该 OK，暂时不提出来
				//System.out.println(red(pixel) - red(last_pixel));
				if(Math.abs(red(pixel) - red(last_pixel)) + Math.abs(blue(pixel) - blue(last_pixel)) + Math.abs(green(pixel) - green(last_pixel)) > 10){
					board_x_sum += Math.round(x+i);
					board_x_c += 1;
				}
			}
			if(board_x_sum>0){
				lx = board_x_sum / board_x_c;
				ly=(int)y;
			}
		}while(lx!=0||ly!=0);
		blx=(int)x;bly=(int)y-2;
		return new int[]{(int)x,(int)y};
	}
	public int[] getcenter(){
		for(int i=0;i<h/2;i++){
			int center_x_sum=0,center_x_c=0;
			for(int u=0;u<w;u++)
				if(getPixel(u,i)==center_color){
					center_x_sum+=u;
					center_x_c++;
				}
			if(center_x_c>0){
				cx=center_x_sum/center_x_c;
				/*int center_y_sum=0,center_y_c=0;
				for(int u=i;u<i+30;u++){
					if(getPixel(cx,u)==center_color){
						center_y_sum+=u;
						center_y_c++;
					}
				}
				if(center_y_c<10)continue;
				cy=center_y_sum/center_y_c;*/
				cy=i+11;
				return new int[]{cx,cy};
			}
		}
		cx=cy=-1;
		return null;
	}
	public boolean colorequle(int col1,int col2,double rate){
		return (Math.abs(red(col1)-red(col2))+Math.abs(green(col1)-green(col2))+Math.abs(blue(col1)-blue(col2)))/3d/255d<rate;
	}
	
	public int[] getpeople(){
		for(int i =0; i < h; i++){
			for(int u=0;u<w;u++){
				if(colorequle(getPixel(u,i),peoplecolor,0.05)&&
				   colorequle(getPixel(u+5,i),peoplecolor,0.05)&&
				   colorequle(getPixel(u,i+5),peoplecolor,0.05)&&
				   colorequle(getPixel(u-5,i),peoplecolor,0.05)&&
				   colorequle(getPixel(u,i-5),peoplecolor,0.05)){
					px=u;py=i;
					return new int[]{u,i};
				}
			}
		}
		return null;
	}
	
    public void detection(Bitmap originalBitmap) {
        //将原图灰度化
        this.temp = originalBitmap;//toGrayscale(originalBitmap);
        //图片的宽高
        w = temp.getWidth();
        h = temp.getHeight();

        //存放灰度图个像素点的数值
        mmap = new int[w * h];
		
        //获取灰度图各像素点的数值，并赋给mmap数组
        temp.getPixels(mmap, 0, temp.getWidth(), 0, 0, temp.getWidth(),
					   temp.getHeight());
		getpeople();
		getblocktop();
		getblockright();
		//if(blx==0||bly==0)
			getblockleft();
		getcenter();
    }

    /**
     *
     * 根据设定的阙值获取处理后的图片
     *
     * @param a
     * @param b
     * @param c
     *
     */
    public Bitmap getBitmap(){

        //将筛选出来的结果生成bitmap
        Bitmap bm =temp.copy(Bitmap.Config.ARGB_8888,true);  //Bitmap.createBitmap(cmap, temp.getWidth(), temp.getHeight(),Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888,true);
        Canvas c=new Canvas(bm);
		Paint pai=new Paint();
		pai.setTextSize(60);
		pai.setColor(Color.GREEN);
		pai.setStyle(Paint.Style.STROKE);
		pai.setStrokeWidth(5);
		c.drawCircle(bx,by,30,pai);
		pai.setColor(Color.RED);
		c.drawCircle(brx,bry,30,pai);
		pai.setColor(0xffca00ff);
		c.drawCircle(blx,bly,30,pai);
		pai.setColor(0xffeb8729);
		c.drawCircle(cx,cy,30,pai);
		pai.setColor(0xff66ccff);
		c.drawCircle(px,py,30,pai);
		c.drawText((int)getdistence()+"",400,300,pai);
		return bm;
    }
	public double getdistence(){
		//if(cx==-1||cy==-1)
			return getlen(bx,bry==0?bly:bry,px,py+people_high);
		//else return getlen(cx,cy,px,py+people_high)-3;
	}
	public double getlen(double x1,double y1,double x2,double y2){
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
    /**
     * 获取第x行第y列的色度
     *
     * @param x      第x行
     * @param y      第y列
     * @param bitmap
     * @return
     */
    private int getPixel(int x, int y) {
        if (x < 0 || x >= w || y < 0 || y >= h) {
            return 0;
        }
        return mmap[x+y*w];
    }
}

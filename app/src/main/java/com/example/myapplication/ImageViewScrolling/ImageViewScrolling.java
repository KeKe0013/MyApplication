package com.example.myapplication.ImageViewScrolling;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;

public class ImageViewScrolling extends FrameLayout {

    private static int ANIMATION_DUR = 50;
    ImageView current_image, next_image;
    int last_result = 0, old_value = 0;
    IEventEnd eventEnd;

    public void setEventEnd(IEventEnd eventEnd){
        this.eventEnd = eventEnd;
    }

    public ImageViewScrolling(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ImageViewScrolling(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.image_view_scrolling, this);
        current_image = getRootView().findViewById(R.id.current_image);
        next_image = getRootView().findViewById(R.id.next_image);

        // 初始化位置設置
        next_image.setTranslationY(getHeight());
    }

    public void setValueRandom(int image, int rotate_count){
        // 使用 ViewPropertyAnimator 進行動畫
        current_image.animate()
                .translationY(-getHeight())
                .setDuration(ANIMATION_DUR)
                .start();

        next_image.setTranslationY(next_image.getHeight());
        next_image.animate()
                .translationY(0)
                .setDuration(ANIMATION_DUR)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {}

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        setImage(current_image, old_value % 42); // 有幾張圖 % 多少
                        current_image.setTranslationY(0);
                        if (old_value != rotate_count) {
                            // 如果 old_value 不等於 rotate_count，就繼續滾動
                            setValueRandom(image, rotate_count);
                            old_value++;
                        } else { // 如果旋轉次數達到
                            last_result = 0;
                            old_value = 0;
                            setImage(next_image, image);
                            eventEnd.eventEnd(image % 42, rotate_count);
                        }
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {}

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {}
                });
    }

    private void setImage(ImageView image_view, int value) {
        if (value == Util.CHINESE) {
            image_view.setImageResource(R.drawable.food1);
        } else if (value == Util.JAPANESE) {
            image_view.setImageResource(R.drawable.food2);
        } else if (value == Util.KOREAN) {
            image_view.setImageResource(R.drawable.food3);
        } else if (value == Util.HONGKONG) {
            image_view.setImageResource(R.drawable.food4);
        } else if (value == Util.THAI) {
            image_view.setImageResource(R.drawable.food5);
        } else if (value == Util.VIETNAMESE) {
            image_view.setImageResource(R.drawable.food6);
        } else if (value == Util.NANYANG) {
            image_view.setImageResource(R.drawable.food7);
        } else if (value == Util.AMERICAN) {
            image_view.setImageResource(R.drawable.food8);
        } else if (value == Util.ITALIAN) {
            image_view.setImageResource(R.drawable.food9);
        } else if (value == Util.FEATURE_MEAL) {
            image_view.setImageResource(R.drawable.food10);
        } else if (value == Util.BRUNCH) {
            image_view.setImageResource(R.drawable.food11);
        } else if (value == Util.AFTERNOON_TEA) {
            image_view.setImageResource(R.drawable.food12);
        } else if (value == Util.LIGHTFOOD) {
            image_view.setImageResource(R.drawable.food13);
        } else if (value == Util.DESSERT) {
            image_view.setImageResource(R.drawable.food14);
        } else if (value == Util.ICECREAM) {
            image_view.setImageResource(R.drawable.food15);
        } else if (value == Util.DRINK) {
            image_view.setImageResource(R.drawable.food16);
        } else if (value == Util.COFFEE) {
            image_view.setImageResource(R.drawable.food17);
        } else if (value == Util.TEA) {
            image_view.setImageResource(R.drawable.food18);
        } else if (value == Util.BOX_LUNCH) {
            image_view.setImageResource(R.drawable.food19);
        } else if (value == Util.HEALTHY_MEAL) {
            image_view.setImageResource(R.drawable.food20);
        } else if (value == Util.SUSHI) {
            image_view.setImageResource(R.drawable.food21);
        }else if (value == Util.NOODLES) {
            image_view.setImageResource(R.drawable.food22);
        }else if (value == Util.BEEF_NOODLES) {
            image_view.setImageResource(R.drawable.food23);
        }else if (value == Util.RAMEN) {
            image_view.setImageResource(R.drawable.food24);
        }else if (value == Util.FOODSTALL) {
            image_view.setImageResource(R.drawable.food25);
        }else if (value == Util.BUFFET) {
            image_view.setImageResource(R.drawable.food26);
        }else if (value == Util.PRIX_FIXE) {
            image_view.setImageResource(R.drawable.food27);
        }else if (value == Util.STIR_FRIED) {
            image_view.setImageResource(R.drawable.food28);
        }else if (value == Util.TEPPANYAKI) {
            image_view.setImageResource(R.drawable.food29);
        }else if (value == Util.HOTPOT) {
            image_view.setImageResource(R.drawable.food30);
        }else if (value == Util.STEAK) {
            image_view.setImageResource(R.drawable.food31);
        }else if (value == Util.HAMBURGER) {
            image_view.setImageResource(R.drawable.food32);
        }else if (value == Util.BBQ) {
            image_view.setImageResource(R.drawable.food33);
        }else if (value == Util.CURRY) {
            image_view.setImageResource(R.drawable.food34);
        }else if (value == Util.SEAFOOD) {
            image_view.setImageResource(R.drawable.food35);
        }else if (value == Util.VEGAN) {
            image_view.setImageResource(R.drawable.food36);
        }else if (value == Util.IZAKAYA) {
            image_view.setImageResource(R.drawable.food37);
        }else if (value == Util.BISTRO) {
            image_view.setImageResource(R.drawable.food38);
        }else if (value == Util.BAR) {
            image_view.setImageResource(R.drawable.food39);
        }else if (value == Util.POSH_RESTAURANT) {
            image_view.setImageResource(R.drawable.food40);
        }else if (value == Util.FAMILY_RESTAURANT) {
            image_view.setImageResource(R.drawable.food41);
        }else if (value == Util.VIEW_RESTAURANT) {
            image_view.setImageResource(R.drawable.food42);
        }

        // 設置標籤以便用於比較結果
        image_view.setTag(value);
        last_result = value;
    }

    public int getValue() {
        return Integer.parseInt(next_image.getTag().toString());
    }
}


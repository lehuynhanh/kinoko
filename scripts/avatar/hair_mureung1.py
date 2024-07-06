# Grandpa Luo : Lead Hair Stylist (2090100)
#   Mu Lung : Mu Lung Hair Salon (250000003)

VIP_HAIR_M = [
    30750, # Black Buddha Fire
    30420, # Black Cozy Amber
    30150, # Black Dreadlocks
    30810, # Black Gruff & Tough
    30240, # Black Monkey
    30710, # Black Puffy Fro
    30370, # Black Shaggy Dragon
    30640, # Black Urban Dragon
]
VIP_HAIR_F = [
    31300, # Black Chantelle
    31180, # Black Cutey Doll
    31910, # Black Housewife
    31460, # Black Lady Mariko
    31160, # Black Lori
    31470, # Black Ming Ming
    31140, # Black Pei Pei
    31660, # Black Tighty Bun
]

HAIR_STYLE_COUPON_VIP = 5150053
HAIR_COLOR_COUPON_VIP = 5151036

answer = sm.askMenu("Welcome to the Mu Lung hair shop. If you have #b#t5150053##k or a #b#t5151036##k allow me to take care of your hairdo. Please choose the one you want.\r\n" + \
        "#L0##bHaircut (VIP coupon)#k#l\r\n" + \
        "#L1##bDye your hair (VIP coupon)#k#l"
)
if answer == 0:
    color = sm.getHair() % 10
    choices = [ hair + color for hair in (VIP_HAIR_M if sm.getGender() == 0 else VIP_HAIR_F) ]
    answer = sm.askAvatar("I can totally change up your hairstyle and make it look so good. Why don't you change it up a bit? With #b#t5150053##k I'll change it for you. Choose the one to your liking~", choices)
    if answer >= 0 and answer < len(choices):
        if sm.removeItem(HAIR_STYLE_COUPON_VIP, 1):
            sm.changeAvatar(choices[answer])
            sm.sayNext("Check it out!! What do you think? Even I think this one is a work of art! AHAHAHA. Please let me know when you want to change your hairstyle again, because I'll make you look good each time!")
        else:
            sm.sayNext("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't give you a haircut without it. I'm sorry...")
elif answer == 1:
    hair = sm.getHair()
    hair = hair - (hair % 10)
    choices = [ hair + i for i in range(8) ]
    answer = sm.askAvatar("I can totally dye your hair and make it look so good. Why don't you change it up a bit? With #b#t5151036##k I'll change it for you. Choose the one to your liking~", choices)
    if answer >= 0 and answer < len(choices):
        if sm.removeItem(HAIR_COLOR_COUPON_VIP, 1):
            sm.changeAvatar(choices[answer])
            sm.sayNext("Check it out!! What do you think? Even I think this one is a work of art! AHAHAHA. Please let me know when you want to dye your hair again, because I'll make you look good each time!")
        else:
            sm.sayNext("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't dye your hair without it. I'm sorry...")

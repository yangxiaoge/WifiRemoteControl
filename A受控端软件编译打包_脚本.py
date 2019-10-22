#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os

# 需要python3.0以上版本
# 项目根目录
print("file_dir ---> "+os.getcwd())
file_dir = os.getcwd()
# release apk路径
release_apk_path = file_dir + r'\app\build\outputs\apk\release\app-release.apk'
# 系统签名后的apk路径，同release_apk_path路径一致，改个名字。
#signed_apk_path = release_apk_path.replace('app-release.apk','signed_release.apk')
signed_apk_path = file_dir + r'\app\build\outputs\apk\release\signed_release.apk'

# 系统签名文件的路径
system_signfile_path = r'D:\workspace\Android\seuic\sign_system_apk_file'


# 进入项目目录
os.chdir(file_dir)
# 打包
print('--------gradlew assembleRelease--------')
os.system('gradlew assembleRelease')

# 系统签名
print('\n--------system sign start--------')
os.system('java -jar '+ system_signfile_path + '\\signapk.jar ' + system_signfile_path + '\\platform.x509.pem '+ system_signfile_path + '\\platform.pk8 '+ release_apk_path + ' '+ signed_apk_path)
print('--------system sign end--------\n')

# 安装apk
print('--------install apk--------')
os.system('adb install -r '+ signed_apk_path)
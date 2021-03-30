# window手动备份

## 1.   修改"setting.txt"文件
​    第一行为备份目的文件夹
​    后若干行为备份源文件夹
​    以下实例为：将 E:\CodeBase\Java\original1 的文件备份到 E:\CodeBase\Java\gpppppppppp\paper1 中
​                           将 E:\CodeBase\Java\original2 的文件备份到 E:\CodeBase\Java\gpppppppppp\paper2 中

以下的内容直接复制到setting.txt文件中  空格之类的不用管 对不对齐也不用管）
```
sync_path       #       E:\CodeBase\Java\gpppppppppp

paper1            #   E:\CodeBase\Java\original1
paper2           #   E:\CodeBase\Java\original2
```

具体解释：
```
sync_path （固定不变）                    #              E:\CodeBase\Java\gpppppppppp  （备份目的路径）

paper1   （备份目的文件夹，不能写成中文）     #              E:\CodeBase\Java\original1  （可以中文）
```

## 2.   "启动.bat" 右键 发送到 桌面快捷方式

## 3.    从此需要备份就打开桌面快捷方式，备份完成关闭黑窗口即可




package com.hzk.arthas.demo;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * 5.8版本，SM4 sm4(byte[] key)
 * 5.6版本，SymmetricCrypto sm4(byte[] key)
 * 本地开发5.8版本，运行时5.6版本，导致NoSuchMethodError: cn.hutool.crypto.SmUtil.sm4([B)Lcn/hutool/crypto/symmetric/SM4;
 * 【演示】
 * 指令：sc、sm、jad
 * sc cn.hutool.crypto.SmUtil
 * sm cn.hutool.crypto.SmUtil sm4
 * jad cn.hutool.crypto.SmUtil sm4
 */
public class HutoolSM4Test {

    public static void main(String[] args) throws Exception {
        try {
            sm4();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.in.read();
    }

    private static void sm4() {
        byte[] encoded = SmUtil.sm4().getSecretKey().getEncoded();
        // version=5.6.5
//        SymmetricCrypto symmetricCrypto = SmUtil.sm4(encoded);
        // version=5.8.31
//        SM4 symmetricCrypto = SmUtil.sm4(encoded);
        String str = "abc";
        byte[] encryptBytes = SmUtil.sm4(encoded).encrypt(str);
        byte[] decryptBytes = SmUtil.sm4(encoded).decrypt(encryptBytes);
        String newStr = new String(decryptBytes);
        boolean flag = str.equals(newStr);
        System.out.println("加解密对称:" + flag);
    }

}

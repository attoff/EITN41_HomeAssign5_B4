import java.math.BigInteger;
import java.util.Base64;
import java.util.Formatter;

/**
 * Created by vikto on 2016-12-18.
 */
public class ASN1 {

    public static void main(String[] args) {
        BigInteger p = new BigInteger("139721121696950524826588106850589277149201407609721772094240512732263435522747938311240453050931930261483801083660740974606647762343797901776568952627044034430252415109426271529273025919247232149498325412099418785867055970264559033471714066901728022294156913563009971882292507967574638004022912842160046962763");
        BigInteger q = new BigInteger("141482624370070397331659016840167171669762175617573550670131965177212458081250216130985545188965601581445995499595853199665045326236858265192627970970480636850683227427420000655754305398076045013588894161738893242561531526805416653594689480170103763171879023351810966896841177322118521251310975456956247827719");
        BigInteger e = new BigInteger("65537");

        byte[] pDer = encode(p);

        System.out.println("The result of " + p + " is \n" + byteToHex(pDer));

        String privateKey = createKey(p, q);
        System.out.println("final " + privateKey);

    }

    private static String createKey(BigInteger p_int, BigInteger q_int) {
        byte[] version, p, q, n, e, d, exp1, exp2, coeff = null;

        BigInteger version_int = new BigInteger("0");
        version = encode(version_int);

        p = encode(p_int);
        q = encode(q_int);

        BigInteger e_int = new BigInteger("65537");
        e = encode(e_int);

        BigInteger n_int = p_int.multiply(q_int);
        n = encode(n_int);

        BigInteger fi = (q_int.subtract(BigInteger.ONE).multiply(p_int.subtract(BigInteger.ONE)));
        BigInteger d_int = e_int.modInverse(fi);
        d = encode(d_int);

        BigInteger exp1_int = d_int.mod(p_int.subtract(BigInteger.ONE));
        exp1 = encode(exp1_int);

        BigInteger exp2_int = d_int.mod(q_int.subtract(BigInteger.ONE));
        exp2 = encode(exp2_int);

        BigInteger coeff_int = q_int.modInverse(p_int);
        coeff = encode(coeff_int);

        byte[] first = hexToByte("30");
        int total_length = (version.length + p.length + q.length + e.length + n.length + d.length + exp1.length + exp2.length + coeff.length);

        BigInteger tmp = new BigInteger(String.valueOf(total_length));
        byte[] length = tmp.toByteArray();

        byte[] result_byte = new byte[first.length + length.length + total_length];
        int pos = 0;
        System.arraycopy(first, 0, result_byte, pos, first.length);
        pos += first.length;
        System.arraycopy(length, 0, result_byte, pos, length.length);
        pos += length.length;
        System.arraycopy(version, 0, result_byte, pos, version.length);
        pos += version.length;
        System.arraycopy(n, 0, result_byte, pos, n.length);
        pos += n.length;
        System.arraycopy(e, 0, result_byte, pos, e.length);
        pos += e.length;
        System.arraycopy(d, 0, result_byte, pos, d.length);
        pos += d.length;
        System.arraycopy(p, 0, result_byte, pos, p.length);
        pos += p.length;
        System.arraycopy(q, 0, result_byte, pos, q.length);
        pos += q.length;
        System.arraycopy(exp1, 0, result_byte, pos, exp1.length);
        pos += exp1.length;
        System.arraycopy(exp2, 0, result_byte, pos, exp2.length);
        pos += exp2.length;
        System.arraycopy(coeff, 0, result_byte, pos, coeff.length);

        System.out.println(byteToHex(result_byte).length()/2);
        return Base64.getEncoder().encodeToString(result_byte);

    }


    private static byte[] encode(BigInteger number) {
        String number_hex = number.toString(16);
        byte[] number_bytes = number.toByteArray();
        int len = number_bytes.length;
        String result = "";
        byte[] result_byte = null;

        if (len > 127) {
            byte[] type = {02};
            byte[] bytelen = null;

            if (len == 0) {
                System.out.println("någonting konstigt, längden på long form är 0...");
            }


            BigInteger tmp = new BigInteger(String.valueOf(len));
            byte[] octetTwo = tmp.toByteArray();
            tmp = new BigInteger(String.valueOf(128+octetTwo.length));
            byte[] octetOne = tmp.toByteArray();

            result_byte = new byte[type.length + octetOne.length + octetTwo.length + number_bytes.length];
            System.arraycopy(type, 0, result_byte, 0, type.length);
            System.arraycopy(octetOne, 0, result_byte, type.length, octetOne.length);
            System.arraycopy(octetTwo, 0, result_byte, type.length + octetOne.length, octetTwo.length);
            System.arraycopy(number_bytes, 0, result_byte, type.length + octetOne.length + octetTwo.length, number_bytes.length);

        } else {
            byte[] bytelen = null;
            byte[] type = {02};

            if (checkIfPaddingIsNeeded(number_hex.substring(0, 1))) {
                result = "02" + Integer.toHexString(len) + number_hex;
                bytelen = new byte[]{(byte) len};
            } else {
                result = "02" + Integer.toHexString(len + 1) + "00" + number_hex;
                len = len + 1;
                bytelen = new byte[]{(byte) len};

                number_hex = "00" + number_hex;
                number_bytes = hexToByte(number_hex);

            }

            result_byte = new byte[type.length + bytelen.length + number_bytes.length];
            System.arraycopy(type, 0, result_byte, 0, type.length);
            System.arraycopy(bytelen, 0, result_byte, type.length, bytelen.length);
            System.arraycopy(number_bytes, 0, result_byte, type.length + bytelen.length, number_bytes.length);
        }

        return result_byte;
    }


    public static byte[] hexToByte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String byteToHex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static boolean checkIfPaddingIsNeeded(String substring) {
        if (!substring.contains("9") || !substring.contains("8") || !substring.contains("a") ||
                !substring.contains("b") || !substring.contains("c") || !substring.contains("d") || !substring.contains("f")) {
            return true;
        } else {
            return false;
        }
    }
}

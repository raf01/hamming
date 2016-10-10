import sun.security.util.BitArray;

import java.io.*;

/**
 * Created by rafael on 07/10/16.
 */
public class HammingCode {
    private File original;
    private File code;
    private int codeDataBlockSize;
    private int nParityBits;

    public HammingCode (File message) throws IOException {
        new HammingCode(message, 8);
    }

    public HammingCode (File message, int dataBlockSize) throws IOException {
        this.codeDataBlockSize = dataBlockSize;
        this.nParityBits = findParityCount();
        this.code = generateCodeFromMessage(message);
    }

    private File generateCodeFromMessage(File message) throws IOException {
        File generatedCode = new File("generatedCode");
        final int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        //byte[] codeBuffer = new byte[bufferSize * nParityBits];
        int noOfBytes = 0;

        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(generatedCode));
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(message));
        while((noOfBytes = reader.read(buffer)) != -1) {

            if(noOfBytes % 2 == 0) {
                // Se houver um número par de bytes no arquivo, vem pra cá
                for(int i = 0; i < noOfBytes; i+=2) {
                    int[] currentByteCode1 = generateCodeFromByte(buffer[i]);
                    int[] currentByteCode2 = generateCodeFromByte(buffer[i+1]);
                    byte[] currentBytes = new byte[3];

                    for(int j = 0; j < currentBytes.length; j++) currentBytes[j] = 0; // Zera bytes
                    final int N = currentByteCode1.length;
                    for(int j = 0; j < 8; j++) {
                        if(currentByteCode1[N - 1 - j] != 0) {
                            currentBytes[0] |= (1 << (7 - j));
                        }
                    }
                    for(int j = 8; j < 12; j++) {
                        if(currentByteCode1[N - 1 - j] != 0) {
                            currentBytes[1] |= (1 << (7 + 8 - j));
                        }
                    }

                    for(int j = 0; j < 4; j++) {
                        if(currentByteCode2[N - 1 - j] != 0) {
                            currentBytes[1] |= (1 << 7 - 4 - j);
                        }
                    }
                    for(int j = 4; j < 12; j++) {
                        if(currentByteCode2[N - 1 - j] != 0) {
                            currentBytes[2] |= (1 << (7 + 4 - j));
                        }
                    }

                    writer.write(currentBytes);

                }
            } else {
                // Se houver um número ímpar de bytes no arquivo, trata o último de maneira especial
                for(int i = 0; i < noOfBytes - 1; i+=2) {

                    int[] currentByteCode1 = generateCodeFromByte(buffer[i]);
                    byte[] currentBytes = new byte[2];
                    final int N = currentByteCode1.length;

                    for(int j = 0; j < currentBytes.length; j++) currentBytes[j] = 0; // Zera bytes
                    for(int j = 0; j < 8; j++) {
                        if(currentByteCode1[N - 1 - j] != 0) {
                            currentBytes[0] |= (1 << (7 - j));
                        }
                    }
                    for(int j = 8; j < 12; j++) {
                        if(currentByteCode1[N - 1 - j] != 0) {
                            currentBytes[1] |= (1 << (7 + 8 - j));
                        }
                    }
                    writer.write(currentBytes);
                }
            }
        }
        writer.close();
        reader.close();
        return generatedCode;
    }

    public void setCode(File code) throws IOException {
        this.code = code;
        processCode(code);
    }

    private void processCode(File code) throws IOException {
        this.original = new File("original");
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(original));
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(code));

        final int bufferSize = 1024;
        byte[] codeBuffer = new byte[bufferSize * nParityBits];
        int noOfBytes;
        while((noOfBytes = input.read(codeBuffer)) != -1) {
            if(noOfBytes % 3 == 0) {
                for(int i = 0; i < noOfBytes; i += 3) {
                    byte[] messageBytes = {0, 0};
                    for(int j = 0; j < 8; j++){
                        int[] firstCodeBlock = extract12BitBlock(codeBuffer[i], codeBuffer[i+1], true);
                        int[] secondCodeBlock = extract12BitBlock(codeBuffer[i+1], codeBuffer[i+2], false);

                    }
                    //codeBuffer[i]
                }
            }
        }

    }

    /**
     * Método que extrai blocos de 12 bits a partir de dois bytes
     * @param b0 byte 0
     * @param b1 byte 1
     * @param firstBitsFlag Se for true, recupera os 12 primeiros bits dos dois bytes recebidos. C.C., os últimos.
     * @return O array de inteiros com os bits recuperados
     * */
    private int[] extract12BitBlock(byte b0, byte b1, boolean firstBitsFlag) {
        int[] recoveredBits = new int[12];
        if(firstBitsFlag) {
            for(int i = 0; i < 8; i++){
                recoveredBits[i] = ((b0 & (1 << (7 - i))) != 0) ? 1 : 0;
            }
            for(int i = 8; i < 12; i++){
                recoveredBits[i] = ((b1 & (1 << (7 + 8 - i))) != 0) ? 1 : 0;
            }
        } else {
            for(int i = 0; i < 4; i++){
                recoveredBits[i] = ((b0 & (1 << (7 - 4 - i))) != 0) ? 1 : 0;
            }
            for(int i = 4; i < 12; i++){
                recoveredBits[i] = ((b1 & (1 << (7 + 4 - i))) != 0) ? 1 : 0;
            }
        }
        return recoveredBits;
    }

    static void receive(int a[], int parity_count) {
        // This is the receiver code. It receives a Hamming code in array 'a'.
        // We also require the number of parity bits added to the original data.
        // Now it must detect the error and correct it, if any.

        int power;
        // We shall use the value stored in 'power' to find the correct bits to check for parity.

        int parity[] = new int[parity_count];
        // 'parity' array will store the values of the parity checks.

        String syndrome = "";
        // 'syndrome' string will be used to store the integer value of error location.

        for(power=0 ; power < parity_count ; power++) {
            // We need to check the parities, the same no of times as the no of parity bits added.

            for(int i=0 ; i < a.length ; i++) {
                // Extracting the bit from 2^(power):

                int k = i+1;
                String s = Integer.toBinaryString(k);
                int bit = ((Integer.parseInt(s))/((int) Math.pow(10, power)))%10;
                if(bit == 1) {
                    if(a[i] == 1) {
                        parity[power] = (parity[power]+1)%2;
                    }
                }
            }
            syndrome = parity[power] + syndrome;
        }
        // This gives us the parity check equation values.
        // Using these values, we will now check if there is a single bit error and then correct it.

        int error_location = Integer.parseInt(syndrome, 2);
        if(error_location != 0) {
            System.out.println("Error is at location " + error_location + ".");
            a[error_location-1] = (a[error_location-1]+1)%2;
            System.out.println("Corrected code is:");
            for(int i=0 ; i < a.length ; i++) {
                System.out.print(a[a.length-i-1]);
            }
            System.out.println();
        }
        else {
            System.out.println("There is no error in the received data.");
        }

        // Finally, we shall extract the original data from the received (and corrected) code:
        System.out.println("Original data sent was:");
        power = parity_count-1;
        for(int i=a.length ; i > 0 ; i--) {
            if(Math.pow(2, power) != i) {
                System.out.print(a[i-1]);
            }
            else {
                power--;
            }
        }
        System.out.println();
    }

    private int findParityCount() {
        // We find the number of parity bits required:
        int i=0, parity_count=0;
        while(i < codeDataBlockSize) {
            // 2^(parity bits) must equal the current position
            // Current position is (number of bits traversed + number of parity bits + 1).
            // +1 is needed since array indices start from 0 whereas we need to start from 1.

            if(Math.pow(2,parity_count) == i + parity_count + 1) {
                parity_count++;
            }
            else {
                i++;
            }
        }
        return parity_count;
    }

    private int[] generateCodeFromByte(byte b) {

        int[] codeFromByte = new int[nParityBits + codeDataBlockSize];
        // Initialize this array with '2' to indicate an 'unset' value in parity bit locations:
        int j = 0, k = 0;
        for(int i = 1; i <= codeFromByte.length ; i++) {
            if(Math.pow(2, j) == i) {
                // Found a parity bit location.
                // Adjusting with (-1) to account for array indices starting from 0 instead of 1.

                codeFromByte[i-1] = 2;
                j++;
            }
            else {
                codeFromByte[k+j] = (b & (1 << k)) != 0 ? 1 : 0;
                k++;
            }
        }
        for(int i = 0; i < nParityBits; i++) {
            // Setting even parity bits at parity bit locations:

            codeFromByte[((int) Math.pow(2, i))-1] = getParity(codeFromByte, i);
        }
        return codeFromByte;
    }

    static int getParity(int b[], int power) {
        int parity = 0;
        for(int i=0 ; i < b.length ; i++) {
            if(b[i] != 2) {
                // If 'i' doesn't contain an unset value,
                // We will save that index value in k, increase it by 1,
                // Then we convert it into binary:

                int k = i+1;
                String s = Integer.toBinaryString(k);

                //Nw if the bit at the 2^(power) location of the binary value of index is 1
                //Then we need to check the value stored at that location.
                //Checking if that value is 1 or 0, we will calculate the parity value.

                int x = ((Integer.parseInt(s))/((int) Math.pow(10, power)))%10;
                if(x == 1) {
                    if(b[i] == 1) {
                        parity = (parity+1)%2;
                    }
                }
            }
        }
        return parity;
    }

}

import java.util.Scanner;

public class SimplifiedDES {

  // S-Box 1 for substitution
  private static final int[][] S_BOX_1 = {
          {14,4,13,1,2,15,11,8,3,10,6,12,5,9,0,7},
          {0,15,7,4,14,2,13,1,10,6,12,11,9,5,3,8},
          {4,1,14,8,13,6,2,11,15,12,9,7,3,10,5,0},
          {15,12,8,2,4,9,1,7,5,11,3,14,10,0,6,13}
  };

  // Straight permutation (P-box)
  private static final int[] P_BOX = {2, 3, 0, 1};

  //Array to hold key
  private static int[] keyArray = new int[6];



  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    //inputArray: array that holds parsed input text
    int[] inputArray = new int[8];
    //array: temporary array to hold input prior to parsing
    char[] array;
    int[] encrypted = new int[8];
    int[] decrypted = new int[8];

    //Plaintext parsing
    System.out.println("Enter plaintext in binary (8 bits): ");
    String input = scanner.nextLine();
    //Input plaintext is too long (maximum 8 bits)
    if(input.length() > 8) {
      System.out.println("Invalid plaintext length (8 bits). Exiting program");
      System.exit(0);
    }
    //Copies input string into char array
    array = input.toCharArray();
    //Iterates through entire char array to convert char to int
    for(int i = 0; i < input.length(); i++) {
      //Input must be in binary format (0's and 1's)
      if(Character.getNumericValue(array[i]) != 1 && Character.getNumericValue(array[i]) != 0) {
        System.out.println("Invalid binary number entered. Exiting program");
      }
        inputArray[i] = Character.getNumericValue(array[i]);
    }

    //Key parsing
    System.out.println("Enter key in binary (6 bits): ");
    String inputKey = scanner.nextLine();
    //Inputted key length is too long (maximum 6)
    if(inputKey.length() > 6) {
      System.out.println("Invalid key length (6 bits). Exiting program");
      System.exit(0);
    }
    //Copies inputted key into char array
    array = inputKey.toCharArray();
    //Iterates through entire char array to convert char to int
    for(int i = 0; i < inputKey.length(); i++) {
      //Input must be in binary format (0's and 1's)
      if(Character.getNumericValue(array[i]) != 1 && Character.getNumericValue(array[i]) != 0) {
        System.out.println("Invalid binary number entered. Exiting program");
        System.exit(0);
      }
        keyArray[i] = Character.getNumericValue(array[i]);
    }

    //Round number parsing
    System.out.println("Enter number of rounds to encrypt: ");
    int rounds = scanner.nextInt();

    //Encryption

    //Don't need to permute key for one round
    if(rounds == 1) {
      encrypted = encrypt(inputArray, keyArray);
      System.out.println("Encrypted (Round 1): ");
      printBits(encrypted);
    } else {
      //Iterates through each round of encryption (if more than one round)
      for (int i = 1; i <= rounds; i++) {
        //Uses inputArray for first round only
        if (i == 1) {
          encrypted = encrypt(inputArray, keyArray);
          keyArray = keyPermutation(keyArray);
          System.out.println("Encrypted (Round 1): ");
          printBits(encrypted);
          continue;
        }
        //Doesn't permute key on last round
        if(i == rounds) {
          encrypted = encrypt(encrypted, keyArray);
          System.out.println("Encrypted (Round " + i + "): ");
          printBits(encrypted);
          continue;
        }
        //For any rounds except first and last
        encrypted = encrypt(encrypted, keyArray);
        keyArray = keyPermutation(keyArray);
        System.out.println("Encrypted (Round " + i + "): ");
        printBits(encrypted);
      }
    }

    //Decryption

    //Doesn't need key de-permutation for one round
    if(rounds == 1) {
      decrypted = decrypt(encrypted, keyArray);
      System.out.println("Decrypted (Round 1): ");
      printBits(decrypted);
    } else {
      //Iterates through each round of decryption (if more than one round)
      for (int i = 1; i <= rounds; i++) {
        //Uses encrypted array for first round only
        if(i == 1) {
          decrypted = decrypt(encrypted, keyArray);
          keyArray = keyDePermutation(keyArray);
          System.out.println("Decrypted (Round 1): ");
          printBits(decrypted);
          continue;
        }
        //Doesn't de-permute key on last round
        if(i == rounds) {
          decrypted = decrypt(decrypted, keyArray);
          System.out.println("Decrypted (Round " + i + "): ");
          printBits(decrypted);
          continue;
        }
        //For any round except first and last
        decrypted = decrypt(decrypted, keyArray);
        keyArray = keyDePermutation(keyArray);
        System.out.println("Decrypted (Round " + i + "): ");
        printBits(decrypted);
      }
    }
  }

  // Function to perform one round of encryption

  /**
   *
   * @param plaintext: inputted binary array to encrypt
   * @param key: inputted key to use for encryption
   * @return: returns encrypted int array
   */
  private static int[] encrypt(int[] plaintext, int[] key) {
    // Step 1: Split the plaintext into two halves, L and R
    int[] L = new int[4];
    int[] R = new int[4];
    System.arraycopy(plaintext, 0, L, 0, 4);
    System.arraycopy(plaintext, 4, R, 0, 4);

    // Step 2: Expand R to 6 bits using the expansion box
    int[] expandedR = expand(R);

    // Step 3: XOR with the key
    int[] xorResult = xor(expandedR, key);

    // Step 4: S-Box substitution
    int[] sBoxResult = sBoxSubstitution(xorResult);

    // Step 5: P-Box permutation
    int[] pBoxResult = pBoxPermutation(sBoxResult);

    // Step 6: XOR with L to get the new R
    int[] newR = xor(L, pBoxResult);

    // Step 7: Update L to the previous R
    int[] newL = R;

    // Combine the new L and new R
    int[] result = new int[8];
    System.arraycopy(newL, 0, result, 0, 4);
    System.arraycopy(newR, 0, result, 4, 4);

    return result;
  }

  // Function to perform one round of decryption

  /**
   *
   * @param ciphertext: inputted binary array to decrypt
   * @param key: inputted binary array to use for decryption
   * @return: returns decrypted binary array
   */
  private static int[] decrypt(int[] ciphertext, int[] key) {
    // The steps for decryption are identical to encryption
    int[] L = new int[4];
    int[] R = new int[4];

    System.arraycopy(ciphertext, 0, R, 0, 4);
    System.arraycopy(ciphertext, 4, L, 0, 4);

    // Step 2: Expand R to 6 bits using the expansion box
    int[] expandedR = expand(R);

    // Step 3: XOR with the key
    int[] xorResult = xor(expandedR, key);

    // Step 4: S-Box substitution
    int[] sBoxResult = sBoxSubstitution(xorResult);

    // Step 5: P-Box permutation
    int[] pBoxResult = pBoxPermutation(sBoxResult);

    // Step 6: XOR with L to get the new R
    int[] newL = xor(L, pBoxResult);

    // Step 7: Update L to the previous R
    int[] newR = R;

    // Combine the new L and new R
    int[] result = new int[8];
    System.arraycopy(newL, 0, result, 0, 4);
    System.arraycopy(newR, 0, result, 4, 4);

    return result;
  }

  // Expand the 4-bit R to 6 bits (Expansion box logic)

  /**
   *
   * @param input: 4 digit binary array
   * @return: returns 6 digit binary array that follows expansion box logic
   */
  private static int[] expand(int[] input) {
    return new int[]{input[0], input[0], input[1], input[2], input[3], input[3]};
  }

  // XOR two bit arrays

  /**
   *
   * @param a: first binary array to use for xor
   * @param b: second binary array to use for xor
   * @return: returns the result of a ^ b
   */
  private static int[] xor(int[] a, int[] b) {
    int[] result = new int[a.length];
    for (int i = 0; i < a.length; i++) {
      result[i] = a[i] ^ b[i];
    }
    return result;
  }

  // Perform S-Box substitution

  /**
   *
   * @param input: 6 digit binary array
   * @return: returns 4 digit binary array that follows S-Box 1 logic
   */
  private static int[] sBoxSubstitution(int[] input) {
    // Row is determined by b1b6, column is b2b3b4b5
    int sBoxValue;
    //Uses bitwise operators to convert binary to integer
    int row = (input[0] << 1) | input[5];
    int column = (input[1] << 3) | (input[2] << 2) | (input[3] << 1) | input[4];
    //Used only if column = 0 since column-1 doesn't work if column = 0
    if(column == 0) {
      sBoxValue = S_BOX_1[row][0];
    } else
    sBoxValue = S_BOX_1[row][column-1];

    // Convert the S-Box value to 4 bits
    return new int[]{(sBoxValue >> 3) & 1, (sBoxValue >> 2) & 1, (sBoxValue >> 1) & 1, sBoxValue & 1};
  }

  // Perform P-Box permutation

  /**
   *
   * @param input: 4 digit binary array
   * @return: returns 4 digit binary array that follows P-Box logic
   */
  private static int[] pBoxPermutation(int[] input) {
    int[] result = new int[4];
    for (int i = 0; i < 4; i++) {
      result[i] = input[P_BOX[i]];
    }
    return result;
  }

  // Utility to print bits

  /**
   *
   * @param bits: binary array to be printed
   */
  private static void printBits(int[] bits) {
    for (int bit : bits) {
      System.out.print(bit);
    }
    System.out.println();
  }

  /**
   *
   * @param key: 6 digit binary array to be permuted according to key permutation logic
   * @return: returns key after permutation
   */
  private static int[] keyPermutation(int[] key) {
    int[] left = new int[3];
    int[] right = new int[3];

    //Splits key into left and right (3 bits each)
    System.arraycopy(key, 0, left, 0, 3);
    System.arraycopy(key, 3, right, 0, 3);

    //Performs left-shift (1 bit)
    int[] newLeft = new int[]{left[1], left[2], left[0]};
    int[] newRight = new int[]{right[1], right[2], right[0]};

    //Performs and returns permuted key
    return new int[]{newRight[1], newRight[2], newLeft[0], newRight[0], newLeft[1], newLeft[2]};
  }

  /**
   *
   * @param key: 6 digit binary array to have permutation reversed
   * @return: returns de-permuted key
   */
  private static int[] keyDePermutation(int[] key) {
    //De-permutes the key
    int[] left = new int[]{key[2], key[4], key[5]};
    int[] right = new int[]{key[3], key[0], key[1]};

    //De-shifts the key
    left = new int[]{left[2], left[0], left[1]};
    right = new int[]{right[2], right[0], right[1]};

    //Combines left and right arrays and returns
    return new int[]{left[0],left[1],left[2],right[0], right[1], right[2]};
  }
}

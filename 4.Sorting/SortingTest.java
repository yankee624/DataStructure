import java.io.*;
import java.util.*;

public class SortingTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try
		{
			boolean isRandom = false;	// 입력받은 배열이 난수인가 아닌가?
			int[] value;	// 입력 받을 숫자들의 배열
			String nums = br.readLine();	// 첫 줄을 입력 받음
			if (nums.charAt(0) == 'r')
			{
				// 난수일 경우
				isRandom = true;	// 난수임을 표시

				String[] nums_arg = nums.split(" ");

				int numsize = Integer.parseInt(nums_arg[1]);	// 총 갯수
				int rminimum = Integer.parseInt(nums_arg[2]);	// 최소값
				int rmaximum = Integer.parseInt(nums_arg[3]);	// 최대값

				Random rand = new Random();	// 난수 인스턴스를 생성한다.

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 각각의 배열에 난수를 생성하여 대입
					value[i] = rand.nextInt(rmaximum - rminimum + 1) + rminimum;
			}
			else
			{
				// 난수가 아닐 경우
				int numsize = Integer.parseInt(nums);

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 한줄씩 입력받아 배열원소로 대입
					value[i] = Integer.parseInt(br.readLine());
			}

			// 숫자 입력을 다 받았으므로 정렬 방법을 받아 그에 맞는 정렬을 수행한다.
			while (true)
			{
				int[] newvalue = (int[])value.clone();	// 원래 값의 보호를 위해 복사본을 생성한다.

				String command = br.readLine();

				long t = System.currentTimeMillis();
				switch (command.charAt(0))
				{
					case 'B':	// Bubble Sort
						newvalue = DoBubbleSort(newvalue);
						break;
					case 'I':	// Insertion Sort
						newvalue = DoInsertionSort(newvalue);
						break;
					case 'H':	// Heap Sort
						newvalue = DoHeapSort(newvalue);
						break;
					case 'M':	// Merge Sort
						newvalue = DoMergeSort(newvalue);
						break;
					case 'Q':	// Quick Sort
						newvalue = DoQuickSort(newvalue);
						break;
					case 'R':	// Radix Sort
						newvalue = DoRadixSort(newvalue);
						break;
					case 'X':
						return;	// 프로그램을 종료한다.
					default:
						throw new IOException("잘못된 정렬 방법을 입력했습니다.");
				}
				if (isRandom)
				{
					// 난수일 경우 수행시간을 출력한다.
					System.out.println((System.currentTimeMillis() - t) + " ms");
				}
				else
				{
					// 난수가 아닐 경우 정렬된 결과값을 출력한다.
					// for (int i = 0; i < newvalue.length; i++)
					// {
					// 	System.out.println(newvalue[i]);
					// }
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
					for(int i = 0; i < newvalue.length; i++) 
					{
						bw.write(String.valueOf(newvalue[i]));
						bw.newLine();
					}
					bw.flush();
				}

			}
		}
		catch (IOException e)
		{
			System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static int[] DoBubbleSort(int[] value)
	{
		int N = value.length;
		for(int i=0; i<N-1; i++) 
		{
			for(int j=0; j<N-1-i; j++) 
			{
				if(value[j] > value[j+1]) 
				{
					// swap consecutive items if their order is wrong
					int temp = value[j];
					value[j] = value[j+1];
					value[j+1] = temp;
				}
			}
		}
		
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static int[] DoInsertionSort(int[] value)
	{
		int N = value.length;
		for(int i=1; i<N; i++) 
		{
			int newNum = value[i];
			
			// Binary search
			int pos = -1;
			int start = 0, end = i-1, mid;
			while(start <= end) 
			{
				mid = (start + end)/2;
				// if found, the position should be next to that item.
				if(value[mid] == newNum) 
				{
					pos = mid+1;
					break;
				}
				else if(value[mid] > newNum) 
				{
					end = mid-1;
				}
				else 
				{
					start = mid+1;
				}
			}
			// if not found, the position should be the position of first larger item.
			if(pos == -1) pos = start;
			
			// Shift in order to make space for the newNum
			for(int j=i; j>=pos+1; j--) 
			{
				value[j] = value[j-1];
			}
			value[pos] = newNum;
			
		}
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static int[] DoHeapSort(int[] value)
	{
		HeapSort(value, value.length-1);
		return (value);
	}
	
	private static void HeapSort(int[] value, int N) 
	{
		// Build heap
		// Index of first node that has a child = (N-1)/2
		for(int i=(N-1)/2; i>=0; i--) 
		{
			percolateDown(value, i, N);
		}
		// Sort by deleting root items
		for(int i=N; i>=1; i--) 
		{
			int tmp = value[i];
			value[i] = value[0];
			value[0] = tmp;
			percolateDown(value, 0, i-1);
		}
	}
	
	/* Move the element down until it satisfies heap property. */
	private static void percolateDown(int[] value, int start, int end) 
	{
		int child = 2*start+1;
		int rightChild = 2*start+2;
		if(child <= end) 
		{
			if(rightChild <= end && value[rightChild] > value[child]) 
			{
				child = rightChild;
			}
			// At this point, child is the larger one of the two children.
			// If heap property isn't satisfied, swap the items and
			// keep percolating down.
			if(value[child] > value[start]) 
			{
				int tmp = value[start];
				value[start] = value[child];
				value[child] = tmp;
				percolateDown(value, child, end);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static int[] DoMergeSort(int[] value)
	{
		MergeSort(value, 0, value.length-1);
		return (value);
	}
	
	/* Perform MergeSort to the array (value[start] ~ value[end]) */
	private static void MergeSort(int[] value, int start, int end) 
	{
		if(start < end) 
		{
			int mid = (start + end) / 2;
			MergeSort(value, start, mid);
			MergeSort(value, mid+1, end);
			merge(value, start, mid, end);
		}
	}
	
	/* Merge the two parts of the array
	 * First part: value[start] ~ value[mid]
	 * Second part: value[mid+1] ~ value[end] 
	 */
	private static void merge(int[] value, int start, int mid, int end) 
	{
		int[] result = new int[end - start + 1]; // Temporary array to hold merged result
		int i = start; // Starting index of first part
		int j = mid+1; // Starting index of second part
		int idx = 0; // Index of the result array
		
		// Copy values of first part and second part in order
		while(i <= mid && j <= end) 
		{
			if(value[i] >= value[j]) 
			{
				result[idx++] = value[j++];
			} else 
			{
				result[idx++] = value[i++];
			}
		}
		// Copy values of the remaining part
		while(i <= mid) 
		{
			result[idx++] = value[i++];
		}
		while(j <= end) 
		{
			result[idx++] = value[j++];
		}
		
		// Move the results to original array (value)
		System.arraycopy(result, 0, value, start, result.length);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static int[] DoQuickSort(int[] value)
	{
		QuickSort(value, 0, value.length-1);
		return (value);
	}
	
	/* Perform QuickSort to the array (value[start] ~ value[end]) */
	private static void QuickSort(int[] value, int start, int end) 
	{
		if(start < end) 
		{
			int pos = partition(value, start, end);
			QuickSort(value, start, pos-1);
			QuickSort(value, pos+1, end);
		}
	}

	/* Partition the specified part of the array (value[start] ~ value[end]).
	 * Use the last item as pivot.
	 */
	private static int partition(int[] value, int start, int end) 
	{
		int pivot = value[end];
		// Keeps track of the index of the first item greater than pivot.
		// This will be the final position of the pivot.
		int pos = start;
		int tmp;
		
		// Skip to the first item greater than pivot.
		while(pos < end && value[pos] <= pivot) pos++;
		
		// From now, if the new item is smaller than pivot, swap with the tracked index,
		// so that items smaller than pivot are grouped in the front of the array.
		for(int i=pos+1; i<end; i++) 
		{
			if(value[i] < pivot) 
			{
				tmp = value[i];
				value[i] = value[pos];
				value[pos] = tmp;
				pos++;	
			}
		}
		// Swap pivot with the item in value[pos]
		value[end] = value[pos];
		value[pos] = pivot;
		return pos;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/* Perform radix sort based on binary representation.*/
	private static int[] DoRadixSort(int[] value) 
	{
		// 256 bit patterns exist (per byte)
		int numPatterns = 256;
		
		// Integer: 32bits -> 4 iterations, each iteration looking at one byte at a time.
		for(int i=0; i<4; i++) 
		{
			int[] newValue = new int[value.length];
			int[] bitPatterns = new int[numPatterns+1];
			Arrays.fill(bitPatterns, 0);
			
			for(int num: value) 
			{	
				int pattern = (num >> 8*i) & 0xff; // extract bit pattern of num
				bitPatterns[pattern + 1]++;
			} // At this point, bitPatterns[j+1] is the number of occurrences of pattern j
			
			for(int j=0; j<numPatterns; j++) 
			{
				bitPatterns[j+1] += bitPatterns[j];
			} // At this point, bitPatterns[j] is the starting position (in array) of bit pattern j

			
			// In most significant byte, 0x00~0x7f is larger than 0x80~0xff (2s complement)
			if(i == 3) 
			{
				int negShift = bitPatterns[0x80];
				int posShift = bitPatterns[numPatterns] - negShift;
				for(int j=0x00; j<=0x7f; j++) 
				{
					bitPatterns[j] += posShift;
				}
				for(int j=0x80; j<=0xff; j++) 
				{
					bitPatterns[j] -= negShift;
				}
			}

			// Put numbers in appropriate position based on bitPatterns
			for(int num: value) 
			{
				int pattern = (num >> 8*i) & 0xff; // extract bit pattern of num
				newValue[bitPatterns[pattern]++] = num;
			}
			value = newValue;
		}
		
		return (value);
	}
	
	/* Perform radix sort based on decimal representation.*/
	private static int[] DoRadixSortDecimal(int[] value) 
	{
		int div = 1;
		int maxNum = Math.abs(value[0]);
		for(int num: value) 
		{
			num = Math.abs(num);
			if(num > maxNum) maxNum = num;
		}
		
		for(int i=0; i<String.valueOf(maxNum).length(); i++) 
		{
			int[] newValue = new int[value.length];
			int[] digits = new int[10+1];
			Arrays.fill(digits, 0);
			
			for(int num: value) 
			{	
				int digit = (Math.abs(num) / div) % 10;
				digits[digit + 1]++;
			} // At this point, digits[i+1] is the number of numbers with digit i
			
			for(int j=0; j<10; j++) 
			{
				digits[j+1] += digits[j];
			} // At this point, digits[i] is the starting position of the number with digit i
			
			// Put numbers in appropriate position based on digits
			for(int num: value) 
			{
				int digit = (Math.abs(num) / div) % 10;
				newValue[digits[digit]++] = num;
			}
			
			value = newValue;
			div *= 10;
		}
			
		int[] newValue = new int[value.length];
		int idx=0;
		// Put negative numbers first, then positive numbers.
		for(int i=value.length-1; i>=0; i--) 
		{
			int num = value[i];
			if(num < 0) newValue[idx++] = num;
		}
		for(int i=0; i<value.length; i++) 
		{
			int num = value[i];
			if(num >= 0) newValue[idx++] = num;
		}	
		
		return (newValue);
	}
	
}	
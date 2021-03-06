class ListNode {
    int val;
    ListNode next = null;
    public ListNode(int val) {
        this.val = val;
    }
}

public class Demo {
    //  删除链表中等于给定值 val 的所有节点
    public ListNode removeElements(ListNode head, int val) {
        // 处理空链表
        if (head == null) {
            return null;
        }
        // 处理非头结点
        // 删除需要找到前一个结点prev和要删除节点node
        ListNode prev = head;
        ListNode node = head.next;
        // 循环比较
        while (node != null) {
            if (node.val == val) {
                // 删除结点
                prev.next = node.next;
                node = prev.next;
            }else{
                prev = node;
                node = node.next;
            }
        }
        // 处理头结点
        if(head.val == val) {
            head = head.next;
        }
        return head;
    }

    // 反转一个链表
    public ListNode reverseList(ListNode head) {
        // 链表为空或者只有一个结点则不需要逆置
        if (head == null){
            return null;
        }
        if (head.next == null) {
            return head;
        }
        // 记录三个节点位置
        ListNode prev = null;
        ListNode cur = head;
        ListNode newHead = null;
        while (cur != null) {
            ListNode next = cur.next;
            if (next == null) {
                newHead = cur;
            }
            cur.next = prev; // 逆置
            prev = cur;
            cur = next;
        }
        return newHead;
    }

    // 给定一个带有头结点 head 的非空单链表，返回链表的中间结点。
    // 如果有两个中间结点，则返回第二个中间结点。
    public ListNode middleNode(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    // 输入一个链表，输出该链表中倒数第k个结点
    public ListNode FindKthToTail(ListNode head,int k) {
        int len = size(head);
        if(head == null || k <= 0 || k > len){
            return null;
        }
        ListNode slow = head;
        ListNode fast = head;
        while (k != 0) {
            fast = fast.next;
            k--;
        }
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }
        return slow;
    }
    public static int size (ListNode head) {
        ListNode node = head;
        int size = 0;
        while (node != null) {
            node = node.next;
            size++;
        }
        return size;
    }

    // 合并两个有序链表
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        // l1 为空链表
        if (l1 == null) {
            return l2;
        }
        // l2 为空链表
        if (l2 == null) {
            return l1;
        }
        ListNode cur1 = l1;
        ListNode cur2 = l2;
        ListNode newHead = new ListNode(-1);
        ListNode newTail = newHead;
        // 循环比较并插入到新链表
        while (cur1 != null && cur2 != null) {
            if (cur1.val < cur2.val) {
                newTail.next = cur1;
                newTail = newTail.next;
                cur1 = cur1.next;
            }else {
                newTail.next = cur2;
                newTail = newTail.next;
                cur2 = cur2.next;
            }
        }
        // 判断循环结束是哪个链表先插完
        if (cur1 == null) {
            newTail.next = cur2;
        }
        if (cur2 == null) {
            newTail.next = cur1;
        }
        return newHead.next;
    }

    // 以给定值分割链表
    public ListNode partition(ListNode pHead, int x) {
        if (pHead == null) {
            return null;
        }
        if (pHead.next == null) {
            // 只有一个元素
            return pHead;
        }
        // 创建两个新的链表
        ListNode smallHead = new ListNode(-1);
        ListNode smallTail = smallHead;
        ListNode bigHead = new ListNode(-1);
        ListNode bigTail = bigHead;
        for (ListNode cur = pHead; cur != null; cur = cur.next) {
            if (cur.val < x) {
                smallTail.next = new ListNode(cur.val);
                smallTail = smallTail.next;
            }else {
                bigTail.next = new ListNode(cur.val);
                bigTail = bigTail.next;
            }
        }
        smallTail.next = bigHead.next;
        return smallHead.next;
    }

    // 排序链表，删除重复的元素，使得每个元素只出现一次
    public ListNode deleteDuplicates(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode cur = head;
        while (cur != null && cur.next != null) {
            if(cur.val == cur.next.val) {
                cur.next = cur.next.next;
            }else {
                cur = cur.next;
            }
        }
        return head;
    }

    // 排序链表，删除所有重复的元素
    public ListNode deleteDuplication(ListNode pHead) {
        // 创建一个新的链表, 用来放置不重复的元素
        ListNode newHead = new ListNode(-1);
        ListNode newTail = newHead;

        ListNode cur = pHead;
        while (cur != null) {
            if (cur.next != null
                    && cur.val == cur.next.val) {
                // 说明 cur 指向的位置已经是重复的节点了
                // 继续往后找 cur, 找到那个不重复的节点的位置
                // 这样做是为了把若干个相同的节点都跳过去
                while (cur.next != null
                        && cur.val == cur.next.val) {
                    cur = cur.next;
                }
                // 循环结束, cur 指向的是这片重复元素的最后一个
                // 再多走一步, cur 指向的就是不重复的元素了
                cur = cur.next;
            } else {
                // 当前这个节点不是重复节点
                // 就把这个节点插入到新链表中
                newTail.next = new ListNode(cur.val);
                newTail = newTail.next;
                cur = cur.next;
            }
        }   // end while
        return newHead.next;
    }

    // 相交链表取交点
    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        int lenA = size(headA);
        int lenB = size(headB);
        if (lenA > lenB) {
            for (int i = 0; i < lenA - lenB; i++) {
                headA = headA.next;
            }
        }else {
            for (int i = 0; i < lenB - lenA; i++) {
                headB = headB.next;
            }
        }
        while (headA != null && headB != null) {
            if (headA == headB) {
                return headA;
            }
            headA = headA.next;
            headB = headB.next;
        }
        return null;
    }
    // 相交链表取交点(大佬写的牛逼代码)
    public ListNode getIntersectionNode2(ListNode headA, ListNode headB) {
        if (headA == null || headB == null) {
            return null;
        }
        ListNode pA = headA, pB = headB;
        while (pA != pB) {
            pA = pA == null ? headB : pA.next;
            pB = pB == null ? headA : pB.next;
        }
        return pA;
    }

    // 判断链表回文
    public boolean chkPalindrome(ListNode A) {
        if (A == null) {
            // 此处只是假设算回文
            return true;
        }
        if (A.next == null) {
            // 只有一个元素, 就是回文
            return true;
        }
        // 1. 找中间节点
        int len = size(A);
        int steps = len / 2;
        ListNode B = A;
        for (int i = 0; i < steps; i++) {
            B = B.next;
        }
        ListNode prev = null;
        ListNode cur = B;
        while (cur != null) {
            ListNode next = cur.next;
            if (next == null) {
                // 使用 B 指向新链表的头部
                B = cur;
            }
            cur.next = prev;
            // 更新 prev, 更新 cur
            prev = cur;
            cur = next;
        }
        // 3. 对比两个链表内容是否相同
        while (B != null) {
            if (A.val != B.val) {
                // 对应元素不相等, 一定不是回文
                return false;
            }
            A = A.next;
            B = B.next;
        }
        return true;
    }

    // 判断链表有环
    public boolean hasCycle(ListNode head) {
        if (head == null) {
            return false;
        }
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
            if (slow == fast) {
                return true;
            }
        }
        return false;
    }

    // 返回入环的第一个结点，没有返回空
    public ListNode detectCycle(ListNode head) {
        if (head == null) {
            return null;
        }
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
            if (slow == fast) {
                break;
            }
        }
        // 循环结束有两种结果
        // 1、不带环
        if (fast == null || fast.next == null) {
            return null;
        }
        // 2、fast 和 slow 已经重合了
        ListNode cur1 = head;
        ListNode cur2 = fast;
        while (cur1 != cur2) {
            cur1 = cur1.next;
            cur2 = cur2.next;
        }
        return cur1;
    }
}





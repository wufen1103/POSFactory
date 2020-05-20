package com.citaq.util;


public class LinkQueue<T>{

    //链的数据结构  
    private class Node{  
        public  T data;  
        public  Node next;  
        //无参构造函数  
        @SuppressWarnings("unused")
		public Node(){}  
          
        public Node(T data,Node next){  
            this.data=data;  
            this.next=next;  
        }  
    }
    
    //队列头指针  
    private Node front;  
    //队列尾指针  
    private Node rear;  
    //队列长度  
    private int size=0;  
      
    public LinkQueue(){  
        Node n=new Node(null,null);  
        n.next=null;  
        front=rear=n;  
    }  
      
    /** 
     * 队列入队尾算法 
     * @param data 
     */  
    public void r_push_queue(T data){  
        //创建一个节点  
        Node s=new Node(data,null);  
        //将队尾指针指向新加入的节点，将s节点插入队尾  
        rear.next=s;  
        rear=s;  
        size++;  
    }  
    
    /**
     * 队列入队头算法
     * @param data
     */
    public void l_push_queue(T data){
    	Node s = new Node(data,null);
    	if(front.next == null){
    		front.next = s;
    		rear = s;
    	}else{
            //暂存队头元素  
            Node p=front.next;  
    		front.next = s;
    		s.next = p;
    	}
    	size++;
    }
    
    /** 
     * 队列出队算法 
     * @return 
     */  
    public  T l_pop_queue(){  
        if(rear==front){  
        	/*
            try {  
                throw new Exception("堆栈为空");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            */
            return null;  
        }else{  
            //暂存队头元素  
            Node p=front.next;  
            T x=p.data;  
            //将队头元素所在节点摘链  
            front.next=p.next;  
            //判断出队列长度是否为1  
            if(p.next==null)  
                rear=front;  
            //删除节点  
            p=null;  
            size--;  
            return  x;  
        }  
    } 
    
    public void clear(){
    	while(l_pop_queue() != null);
    }
      
    /** 
     * 队列长度
     * @return 
     */  
    public int size(){  
        if(size == 0)return -1;
    	return size;  
    }  
      
    /** 
     * 判断队列是否为空 
     * @return 
      */  
    public  boolean isEmpty(){  
        return  size==0;            
    }  
      
      
    public String toString() {  
        if(isEmpty()){  
            return "[]";  
        }else{  
            StringBuilder sb = new StringBuilder("[");  
            for(Node current=front.next;current!=null;current=current.next){  
                sb.append(current.data.toString() + ", ");  
            }  
            int len = sb.length();  
            return sb.delete(len - 2, len).append("]").toString();  
        } 	

    }
}

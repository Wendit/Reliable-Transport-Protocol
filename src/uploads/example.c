#include "daemon.h"
static mutex_info_node* mutex_list_head = NULL;
static mutex_info_node* mutex_list_tail = NULL;
/*------------------------------ syscalls -------------------------------------------
 *
 *
 *------------------------------------------------------------------------------------
 */
asmlinkage long sys_add_mutex_record(int id)
{
  if (!mutex_list_head) //first thread acquires first lock, allow, no waiting thread
    {
      mutex_list_head = (mutex_info_node*)vmalloc(sizeof(mutex_info_node));
      mutex_list_head -> mutex_id = id;
      mutex_list_head -> owner_tid = -1;
      mutex_list_head -> waiting_q_head = NULL;
      mutex_list_head -> next = NULL;
      mutex_list_tail = mutex_list_head;
      return 0;
    }
  mutex_info_node* new_mutex_entry = (mutex_info_node*)vmalloc(sizeof(mutex_info_node));
  new_mutex_entry -> mutex_id = id;
  new_mutex_entry -> owner_tid = -1;
  new_mutex_entry -> waiting_q_head = NULL;
  mutex_list_tail -> next = new_mutex_entry;
  mutex_list_tail = new_mutex_entry;
  return 0;
}
asmlinkage long sys_remove_mutex_record(int id)
{
  if (mutex_list_head -> mutex_id == id)
    {
      mutex_info_node* temp = mutex_list_head;
      mutex_list_head = mutex_list_head -> next;
      free_mutex_record(temp);
      return 0;
    }
  mutex_info_node* curr = mutex_list_head;
  mutex_info_node* prev = NULL;
  while (curr)
    {
      if (curr -> mutex_id == id)
	{
	  prev -> next = curr -> next;
	  free_mutex_record(curr);
	  return 0;
	}
      prev = curr;
      curr = curr -> next;
    }
  return 0;
}
asmlinkage long sys_check_deadlock(int req_mutex_id, long curr_tid)
{
  // printk("current thread id: %lu\n", curr_tid);
  // printk("request mutex id: %lu\n", req_mutex_id);
 https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA%3D%3D
  1/511/12/2015 https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA...
  //check deadlock
  //get mutex that the requesting thread currently holds
  mutex_info_node* mutex_entry = get_mutex_entry_from_tid(curr_tid);
  if (mutex_entry)
    {
      //get owner tid for the requested mutex
      mutex_info_node* requested_mutex = get_mutex_entry_from_mutex_id(req_mutex_id);
      if (requested_mutex)
	{
	  long owner_tid = requested_mutex -> owner_tid;
	  //check if owner_tid is in the wait queue
	  int waiting = in_wait_queue(mutex_entry, owner_tid);
	  if (waiting)
	    return 1;
	}
    }
  //no deadlock
  // printk(">>>>>>>>>>>>>>>>\n");
  // print_info();
  // printk("<<<<<<<<<<<<<<<<\n");
  mutex_info_node* req_mutex_entry = get_mutex_entry_from_mutex_id(req_mutex_id);
  //add current thread to wait queue of the required mutex
  add_my_wait_queue(curr_tid, req_mutex_entry);
  // print_info();
  return 0;
}
asmlinkage long sys_update_mutex_owner(int mutex_id, unsigned long owner_tid)
{
  mutex_info_node* mutex_entry = get_mutex_entry_from_mutex_id(mutex_id);
  if (mutex_entry)
    {
      mutex_entry -> owner_tid = owner_tid;
      delete_waiting_q_node(mutex_entry, owner_tid);
      return 0;
    }
  mutex_entry = (mutex_info_node*)vmalloc(sizeof(mutex_info_node));
  mutex_entry -> mutex_id = mutex_id;
  mutex_entry -> owner_tid = owner_tid;
  mutex_entry -> waiting_q_head = NULL;
  mutex_entry -> next = mutex_list_head;
  mutex_list_tail -> next = mutex_entry;
  mutex_list_tail = mutex_entry;
  return 0;
}
asmlinkage long sys_remove_mutex_owner(int mutex_id, unsigned long owner_tid)
{
  mutex_info_node* mutex_entry = get_mutex_entry_from_mutex_id(mutex_id);
  if (mutex_entry && mutex_entry -> owner_tid == owner_tid)
    mutex_entry -> owner_tid = -1;
  return 0;
}
asmlinkage long sys_get_mutex_id_from_tid(long tid)
{
  mutex_info_node* curr = mutex_list_head;
  while (curr)
    {
      if (tid == (curr -> owner_tid))
      https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA%3D%3D
	2/511/12/2015 https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA...
	{
	  return curr -> mutex_id;
	}
      curr = curr -> next;
    }
  return -1;
}
/*------------------------------ helper functions -----------------------------------
 *
 *
 *------------------------------------------------------------------------------------
 */
mutex_info_node* get_mutex_entry_from_tid(long tid)
{
  mutex_info_node* curr = mutex_list_head;
  while (curr)
    {
      if (tid == (curr -> owner_tid))
	{
	  return curr;
	}
      curr = curr -> next;
    }
  return NULL;
}
mutex_info_node* get_mutex_entry_from_mutex_id(int mutex_id)
{
  mutex_info_node* curr = mutex_list_head;
  while (curr)
    {
      if (mutex_id == (curr -> mutex_id))
	{
	  return curr;
	}
      curr = curr -> next;
    }
  return NULL;
}
int in_wait_queue(mutex_info_node* mutex_entry, unsigned long owner_tid)
{
  waiting_q_node* curr = mutex_entry -> waiting_q_head;
  while (curr)
    {
      if (curr -> tid == owner_tid)
	{
	  return 1;
	}
      curr = curr -> next;
    }
  return 0;
}
void add_my_wait_queue(long tid, mutex_info_node* mutex_entry)
{
  // if (mutex_entry -> owner_tid != tid)
  // {
  waiting_q_node* head = mutex_entry -> waiting_q_head;
  if (!head)
  https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA%3D%3D
    3/511/12/2015 https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA...
    {
      //allocate new head
      head = (waiting_q_node*)vmalloc(sizeof(waiting_q_node));
      head -> tid = tid;
      head -> next = NULL;
      mutex_entry -> waiting_q_head = head;
      return;
    }
  waiting_q_node* curr = head;
  // no duplicate entries in waiting queue
  while (curr)
    {
      if (curr -> tid == tid)
	{
	  return;
	}
      curr = curr -> next;
    }
  //allocate new head
  waiting_q_node* new_head = (waiting_q_node*)vmalloc(sizeof(waiting_q_node));
  new_head -> tid = tid;
  new_head -> next = head;
  mutex_entry -> waiting_q_head = new_head;
  // }
}
void delete_waiting_q_node(mutex_info_node* mutex_entry, unsigned long tid)
{
  waiting_q_node* head = mutex_entry -> waiting_q_head;
  if (!head) //no other threads waiting -- do nothing
    return;
  if (head -> tid == tid)
    {
      mutex_entry -> waiting_q_head = head -> next;
      vfree(head);
    }
  else
    {
      waiting_q_node* prev = NULL;
      waiting_q_node* curr = head;
      while (curr)
	{
	  if (curr -> tid == tid)
	    {
	      prev -> next = curr -> next;
	      vfree(curr);
	      return;
	    }
	  prev = curr;
	  curr = curr -> next;
	}
    }
}
void free_mutex_record(mutex_info_node* mutex_entry)
{
  waiting_q_node* curr = mutex_entry -> waiting_q_head;
  while (curr)
    {
      waiting_q_node* next = curr -> next;
      vfree(curr);
    https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA%3D%3D
      4/511/12/2015 https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA...
      curr = next;
    }
  vfree(mutex_entry);
}
void print_info(void)
{
  printk("############## BEGIN ###################\n");
  mutex_info_node* curr = mutex_list_head;
  while (curr)
    {
      printk("--------------------------\n");
      printk("mutex: %d\n", curr -> mutex_id);
      printk("owner thread: %lu\n", curr -> owner_tid);
      waiting_q_node* wait_q = curr -> waiting_q_head;
      printk("waiting queue:\n");
      while (wait_q)
	{
	  printk("thread: %lu\n", wait_q -> tid);
	  wait_q = wait_q -> next;
	}
      curr = curr -> next;
    }
  printk("############### END ##################\n");
}
https://github.gatech.edu/raw/hli362/CS3210-Fall2015/master/Project2/kernel/daemon.c?token=AAADlqQ0LQ4KkSbD2jbSSWDRPBTmrmjpks5WTgh_wA%3D%3D
5/5

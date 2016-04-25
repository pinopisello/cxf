delete from cxf_rm_dest_sequences;
delete from cxf_rm_inbound_attachments;
delete from cxf_rm_inbound_messages;
delete from cxf_rm_outbound_attachments;
delete from cxf_rm_outbound_messages;
delete from cxf_rm_src_sequences;


select * from cxf_rm_outbound_messages
select * from cxf_rm_dest_sequences

select encode(cxf_rm_inbound_messages.content,'escape') from cxf_rm_inbound_messages

select encode(cxf_rm_outbound_messages.content,'escape') from cxf_rm_outbound_messages

select LAST_MSG_NO,encode(ACKNOWLEDGED,'escape') from CXF_RM_DEST_SEQUENCES




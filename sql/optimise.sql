-- first, add whatever indices you need. Note that here having many and correct indices is even more important than for a normal database.
pragma journal_mode = delete; -- to be able to actually set page size
pragma page_size = 4096; -- trade off of number of requests that need to be made vs overhead. 

-- insert into ftstable(ftstable) values ('optimize'); -- for every FTS table you have (if you have any)

vacuum; -- reorganize database and apply changed page size
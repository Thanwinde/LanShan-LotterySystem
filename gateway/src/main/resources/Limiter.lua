local resourceId = ARGV[1]
local QPS = tonumber(ARGV[2])

local current_time = redis.call('TIME')
local unix_timestamp = tonumber(current_time[1])
local milliseconds = tonumber(current_time[2])
local now = unix_timestamp * 1000 + milliseconds / 1000
local star = now - 1000

redis.call('ZREMRANGEBYSCORE',resourceId,0,star)

local cnt = redis.call('ZCARD',resourceId)

if cnt == false then
    cnt = 0
end

if cnt >= QPS then
    return false
end

redis.call('ZADD',resourceId,now,now)

redis.call('EXPIRE',resourceId,3)

return true

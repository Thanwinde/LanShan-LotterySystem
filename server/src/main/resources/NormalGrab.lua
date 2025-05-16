local lotteryId = ARGV[1]
local userId = ARGV[2]
local top = tonumber(ARGV[3])
local poolKey = 'prize:pool:' .. lotteryId
local CountTableId = 'lottery:count:' .. lotteryId
local RecordTable = 'lottery:record:'.. lotteryId

if redis.call('HEXISTS',CountTableId,userId) == 0 then
    redis.call('HSET',CountTableId,userId,0)
end



local cnt = tonumber(redis.call('HGET',CountTableId,userId))

if cnt >= top then
    return '"-1#0#full"'
end

local prize = redis.call('SPOP',poolKey)

if prize == false then
    return '"-2#0#null"'
end

redis.call('HINCRBY', CountTableId, userId , 1)

redis.call('HSET',RecordTable,prize,userId)

return prize




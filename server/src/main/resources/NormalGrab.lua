local lotteryId = ARGV[1]
local userId = ARGV[2]
local top = tonumber(ARGV[3])
local black = tonumber(ARGV[4])
local fortune = tonumber(ARGV[5])

local poolKey = 'prize:pool:' .. lotteryId
local CountTableId = 'lottery:count:' .. lotteryId
local RecordTable = 'lottery:record:'.. lotteryId

if redis.call('HEXISTS',CountTableId,userId) == 0 then
    redis.call('HSET',CountTableId,userId,0)
end



local cnt = tonumber(redis.call('HGET',CountTableId,userId))

if cnt >= top then
    return '"-1#0#full#0#"'
end

if(black == 1) then
    redis.call('HINCRBY', CountTableId, userId , 1)
    local prize1 = redis.call('SPOP',poolKey)
    local prize2 = redis.call('SPOP',poolKey)
    if prize1 == false then
        redis.call('HSET',RecordTable,'"-2#0#null#0#"',userId)
        return '"-2#0#null#0#"'
    end
    if prize2 == false then
        redis.call('HSET',RecordTable,prize1,userId)
        return prize1
    end

    local component1 = {}
    local component2 = {}

    for part in string.gmatch(prize1, "[^#]+") do
        table.insert(component1, part)
    end

    for part in string.gmatch(prize2, "[^#]+") do
        table.insert(component2, part)
    end
    local len1 = #component1
    local len2 = #component2

    if(tonumber(component1[len1 - 1]) > tonumber(component2[len2 - 1])) then
        redis.call('HSET',RecordTable,prize1,userId)
        redis.call('SADD',poolKey,prize2)
        return prize1
    else
        redis.call('HSET',RecordTable,prize2,userId)
        redis.call('SADD',poolKey,prize1)
        return prize2
    end
end

if(fortune == 1) then
    redis.call('HINCRBY', CountTableId, userId , 1)
    local prize1 = redis.call('SPOP',poolKey)
    local prize2 = redis.call('SPOP',poolKey)
    if prize1 == false then
        redis.call('HSET',RecordTable,'"-2#0#null#0#"',userId)
        return '"-2#0#null#0#"'
    end
    if prize2 == false then
        redis.call('HSET',RecordTable,prize1,userId)
        return prize1
    end

    local component1 = {}
    local component2 = {}

    for part in string.gmatch(prize1, "[^#]+") do
        table.insert(component1, part)
    end

    for part in string.gmatch(prize2, "[^#]+") do
        table.insert(component2, part)
    end
    local len1 = #component1
    local len2 = #component2

    if(tonumber(component1[len1 - 1]) > tonumber(component2[len2 - 1])) then
        redis.call('HSET',RecordTable,prize2,userId)
        redis.call('SADD',poolKey,prize1)
        return prize2
    else
        redis.call('HSET',RecordTable,prize1,userId)
        redis.call('SADD',poolKey,prize2)
        return prize1
    end
end

local prize = redis.call('SPOP',poolKey)

if prize == false then
    redis.call('HSET',RecordTable,'"-2#0#null#0#"',userId)
    return '"-2#0#null#0#"'
end

redis.call('HINCRBY', CountTableId, userId , 1)

redis.call('HSET',RecordTable,prize,userId)

return prize




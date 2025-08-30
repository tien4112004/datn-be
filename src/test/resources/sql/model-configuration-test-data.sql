-- Test data for ModelConfiguration integration tests
-- This script demonstrates how to provide test data via SQL scripts in integration tests

INSERT INTO model_configuration (model_name, display_name, provider, is_enabled, is_default) VALUES 
('gpt-3.5-turbo-test', 'GPT-3.5 Turbo Test', 'OpenAI', true, false),
('gpt-4-test-script', 'GPT-4 Test Script', 'OpenAI', true, true),
('gemini-pro-test-script', 'Gemini Pro Test Script', 'Google', true, false),
('claude-3-test', 'Claude 3 Test', 'Anthropic', false, false);

-- You can also add more complex test scenarios
-- For example, testing edge cases or specific data configurations
INSERT INTO model_configuration (model_name, display_name, provider, is_enabled, is_default) VALUES 
('test-model-with-long-name-to-verify-varchar-limits', 'Very Long Display Name For Testing Maximum Length Constraints', 'TestProvider', true, false);
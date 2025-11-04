import React from 'react';

const exampleCommands = [
  "Add $50 expense for groceries to Food category",
  "Create a $300 budget for Transportation", 
  "I received $3000 salary today",
  "Show me my Food category spending this month",
  "Set up a $200 Entertainment budget with 75% alert",
  "How much have I spent so far?",
  "Add another $20 to that category",
  "What was my budget for that again?"
];

const conversationExamples = [
  {
    title: "💬 Conversation Memory Examples",
    examples: [
      "User: \"I spent $50 on groceries\"",
      "AI: \"Added $50 expense for groceries...\"",
      "User: \"Add another $25 to that\"",
      "AI: \"I'll add $25 more to groceries...\""
    ]
  }
];

export const ExampleCommands: React.FC = () => {
  return (
    <div className="p-6 flex-1 overflow-y-auto">
      <h3 className="text-lg font-semibold text-gray-800 mb-3">💡 Example Commands</h3>
      <div className="space-y-2 text-sm text-gray-600 mb-6">
        {exampleCommands.map((command, index) => (
          <div key={index} className="p-2 bg-gray-50 rounded text-xs">
            "{command}"
          </div>
        ))}
      </div>

      {conversationExamples.map((section, sectionIndex) => (
        <div key={sectionIndex} className="mb-4">
          <h4 className="text-sm font-semibold text-gray-700 mb-2">{section.title}</h4>
          <div className="space-y-1">
            {section.examples.map((example, index) => (
              <div key={index} className="text-xs text-gray-500 pl-2 border-l-2 border-blue-200">
                {example}
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};